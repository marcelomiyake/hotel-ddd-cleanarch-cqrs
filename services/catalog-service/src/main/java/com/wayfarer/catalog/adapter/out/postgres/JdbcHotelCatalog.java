package com.wayfarer.catalog.adapter.out.postgres;

import com.wayfarer.catalog.application.HotelCatalog;
import com.wayfarer.catalog.domain.HotelCard;
import com.wayfarer.catalog.domain.HotelDetails;
import com.wayfarer.catalog.domain.RoomOffer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcHotelCatalog implements HotelCatalog {
    private static final String HOTEL_CARD_SQL = """
            SELECT h.id, h.name, h.city, h.country, h.address, h.stars, h.guest_rating,
                   h.review_count, h.currency, h.tagline, h.hero_image_url, h.highlights,
                   h.featured, MIN(r.price_per_night_cents) AS price_from_cents
            FROM hotels h JOIN room_types r ON r.hotel_id = h.id
            WHERE (? IS NULL OR h.city ILIKE '%' || ? || '%')
            GROUP BY h.id
            ORDER BY h.featured DESC, h.guest_rating DESC, h.name
            """;
    private static final String HOTEL_BY_ID_SQL = """
            SELECT h.id, h.name, h.city, h.country, h.address, h.stars, h.guest_rating,
                   h.review_count, h.currency, h.tagline, h.hero_image_url, h.highlights,
                   h.featured, MIN(r.price_per_night_cents) AS price_from_cents
            FROM hotels h JOIN room_types r ON r.hotel_id = h.id
            WHERE h.id = ?
            GROUP BY h.id
            """;
    private static final RowMapper<HotelCard> CARD_MAPPER = JdbcHotelCatalog::mapCard;
    private static final RowMapper<RoomOffer> ROOM_MAPPER = JdbcHotelCatalog::mapRoom;

    private final JdbcTemplate jdbc;

    public JdbcHotelCatalog(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<HotelCard> search(String city) {
        return jdbc.query(HOTEL_CARD_SQL, CARD_MAPPER, city, city);
    }

    @Override
    public Optional<HotelDetails> findById(String hotelId) {
        List<HotelCard> cards = jdbc.query(HOTEL_BY_ID_SQL, CARD_MAPPER, hotelId);
        if (cards.isEmpty()) {
            return Optional.empty();
        }
        List<RoomOffer> rooms = jdbc.query("""
                SELECT id, name, description, bed_summary, max_guests, price_per_night_cents,
                       currency, amenities
                FROM room_types WHERE hotel_id = ? ORDER BY price_per_night_cents, name
                """, ROOM_MAPPER, hotelId);
        String[] location = jdbc.queryForObject(
                "SELECT description, location_summary, gallery_urls FROM hotels WHERE id = ?",
                (result, row) -> new String[]{result.getString(1), result.getString(2), result.getString(3)},
                hotelId);
        return Optional.of(new HotelDetails(
                cards.getFirst(), location[0], location[1], split(location[2]), rooms));
    }

    private static HotelCard mapCard(ResultSet result, int row) throws SQLException {
        return new HotelCard(
                result.getString("id"), result.getString("name"), result.getString("city"),
                result.getString("country"), result.getString("address"), result.getInt("stars"),
                result.getBigDecimal("guest_rating"), result.getInt("review_count"),
                result.getLong("price_from_cents"), result.getString("currency"),
                result.getString("tagline"), result.getString("hero_image_url"),
                split(result.getString("highlights")), result.getBoolean("featured"));
    }

    private static RoomOffer mapRoom(ResultSet result, int row) throws SQLException {
        return new RoomOffer(
                result.getString("id"), result.getString("name"), result.getString("description"),
                result.getString("bed_summary"), result.getInt("max_guests"),
                result.getLong("price_per_night_cents"), result.getString("currency"),
                split(result.getString("amenities")));
    }

    private static List<String> split(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split("\\|"))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .toList();
    }
}
