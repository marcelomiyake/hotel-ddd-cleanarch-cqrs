package com.wayfarer.catalog;

import com.wayfarer.catalog.adapter.in.web.HotelController;
import com.wayfarer.catalog.adapter.in.web.HotelDetailsResponse;
import com.wayfarer.catalog.application.GetHotelDetailsHandler;
import com.wayfarer.catalog.application.SearchHotelsHandler;
import com.wayfarer.catalog.application.SearchHotelsQuery;
import com.wayfarer.catalog.domain.HotelCard;
import com.wayfarer.catalog.domain.HotelDetails;
import com.wayfarer.catalog.domain.HotelNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CatalogIntegrationTest {
    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl() + "&currentSchema=hotel_catalog");
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private HotelController controller;

    @Autowired
    private SearchHotelsHandler searchHotels;

    @Autowired
    private GetHotelDetailsHandler getHotelDetails;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void readsSeededHotelsAndRoomOffersFromPostgres() {
        var hotels = searchHotels.handle(new SearchHotelsQuery("Rio de Janeiro"));
        assertThat(hotels).hasSize(4).extracting(HotelCard::city).containsOnly("Rio de Janeiro");
        assertThat(hotels.getFirst().priceFromCents()).isPositive();

        HotelDetails details = getHotelDetails.handle("rio-casa-do-mar");
        assertThat(details.rooms()).hasSize(2);
        assertThat(details.rooms().getFirst().id()).isEqualTo("room-casa-mar-terrace");
        assertThat(details.gallery()).contains("/hotels/casa-mar.webp", "/hotels/casa-mar-room.webp");
    }

    @Test
    void mapsWebResponsesAndAnEmptyHotelCollection() {
        insertEmptyHotel();
        assertThat(controller.search("a city that is not listed")).isEmpty();
        HotelDetailsResponse response = controller.details("empty-hotel");
        assertThat(response.hotel().id()).isEqualTo("empty-hotel");
        assertThat(response.hotel().highlights()).isEmpty();
        assertThat(response.gallery()).isEmpty();
        assertThat(response.rooms()).hasSize(1);
    }

    @Test
    void reportsMissingAndInvalidHotelIdentifiers() {
        assertThatThrownBy(() -> getHotelDetails.handle("missing-hotel"))
                .isInstanceOf(HotelNotFoundException.class);
        assertThatThrownBy(() -> getHotelDetails.handle(" "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> getHotelDetails.handle(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private void insertEmptyHotel() {
        jdbc.update("""
                INSERT INTO hotels (id, name, city, country, address, stars, guest_rating, review_count, currency,
                    tagline, description, location_summary, hero_image_url, gallery_urls, highlights, featured)
                VALUES ('empty-hotel', 'Empty Hotel', 'Test City', 'Brazil', 'Test Address', 3, 8.0, 0, 'BRL',
                    'A simple stay.', 'A test hotel.', 'Test City center', '/hotels/hero.webp', '', '', FALSE)
                """);
        jdbc.update("""
                INSERT INTO room_types (id, hotel_id, name, description, bed_summary, max_guests,
                    price_per_night_cents, currency, amenities)
                VALUES ('empty-room', 'empty-hotel', 'Simple Room', 'A simple room.', '1 queen bed', 2, 50000,
                    'BRL', '')
                """);
    }
}
