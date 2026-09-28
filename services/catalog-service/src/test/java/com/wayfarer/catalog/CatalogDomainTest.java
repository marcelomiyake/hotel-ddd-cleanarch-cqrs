package com.wayfarer.catalog;

import com.wayfarer.catalog.adapter.in.web.CatalogExceptionHandler;
import com.wayfarer.catalog.adapter.in.web.HotelCardResponse;
import com.wayfarer.catalog.adapter.in.web.HotelDetailsResponse;
import com.wayfarer.catalog.application.GetHotelDetailsHandler;
import com.wayfarer.catalog.application.HotelCatalog;
import com.wayfarer.catalog.application.SearchHotelsHandler;
import com.wayfarer.catalog.application.SearchHotelsQuery;
import com.wayfarer.catalog.domain.HotelCard;
import com.wayfarer.catalog.domain.HotelDetails;
import com.wayfarer.catalog.domain.HotelNotFoundException;
import com.wayfarer.catalog.domain.RoomOffer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogDomainTest {
    private static final HotelCard HOTEL = new HotelCard("hotel-1", "Test Hotel", "Rio", "Brazil",
            "Beach Road", 5, new BigDecimal("9.5"), 20, 12000, "BRL", "A calm place.",
            "/hotels/hero.webp", List.of("Pool"), true);
    private static final RoomOffer ROOM = new RoomOffer("room-1", "Ocean Room", "A bright room.",
            "1 king bed", 2, 12000, "BRL", List.of("Ocean view"));
    private static final HotelDetails DETAILS = new HotelDetails(HOTEL, "A thoughtful hotel.",
            "Close to the beach", List.of("/hotels/hero.webp"), List.of(ROOM));

    @Test
    void validatesCatalogValuesAndProtectsCollectionState() {
        assertThatThrownBy(() -> new HotelCard("h", "Hotel", "Rio", "Brazil", "Address", 6,
                new BigDecimal("9.0"), 0, 0, "BRL", "Tag", "/img.webp", List.of(), false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HotelCard("h", "Hotel", "Rio", "Brazil", "Address", 4,
                new BigDecimal("11.0"), 0, 0, "BRL", "Tag", "/img.webp", List.of(), false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new RoomOffer("r", "Room", "Description", "Bed", 0, 100, "BRL", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> HOTEL.highlights().add("Added later"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> DETAILS.gallery().clear())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void normalizesSearchAndReturnsReadModels() {
        HotelCatalog catalog = new InMemoryCatalog();
        SearchHotelsHandler search = new SearchHotelsHandler(catalog);
        assertThat(new SearchHotelsQuery("  Rio  ").city()).isEqualTo("Rio");
        assertThat(new SearchHotelsQuery("  ").city()).isNull();
        assertThat(search.handle(new SearchHotelsQuery(null))).containsExactly(HOTEL);
        assertThatThrownBy(() -> search.handle(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void handlesHotelDetailsFoundMissingAndInvalidIds() {
        GetHotelDetailsHandler handler = new GetHotelDetailsHandler(new InMemoryCatalog());
        assertThat(handler.handle(" hotel-1 ")).isEqualTo(DETAILS);
        assertThatThrownBy(() -> handler.handle("missing")).isInstanceOf(HotelNotFoundException.class);
        assertThatThrownBy(() -> handler.handle(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void mapsDomainReadModelsToApiAndReturnsNotFoundProblem() {
        assertThat(HotelCardResponse.from(HOTEL).guestRating()).isEqualTo(new BigDecimal("9.5"));
        HotelDetailsResponse response = HotelDetailsResponse.from(DETAILS);
        assertThat(response.rooms()).hasSize(1);
        assertThat(response.rooms().getFirst().id()).isEqualTo(ROOM.id());
        assertThatThrownBy(() -> response.rooms().clear()).isInstanceOf(UnsupportedOperationException.class);
        assertThat(new CatalogExceptionHandler().hotelNotFound(new HotelNotFoundException("bad")).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    private static final class InMemoryCatalog implements HotelCatalog {
        @Override
        public List<HotelCard> search(String city) {
            return city == null || HOTEL.city().equalsIgnoreCase(city) ? List.of(HOTEL) : List.of();
        }

        @Override
        public Optional<HotelDetails> findById(String hotelId) {
            return HOTEL.id().equals(hotelId) ? Optional.of(DETAILS) : Optional.empty();
        }
    }
}
