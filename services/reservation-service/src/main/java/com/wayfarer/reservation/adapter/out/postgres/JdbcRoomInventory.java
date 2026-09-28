package com.wayfarer.reservation.adapter.out.postgres;

import com.wayfarer.reservation.application.RoomInventory;
import com.wayfarer.reservation.domain.RoomAvailability;
import com.wayfarer.reservation.domain.RoomUnavailableException;
import com.wayfarer.reservation.domain.StayPeriod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcRoomInventory implements RoomInventory {
    private static final String AVAILABILITY_SQL = """
            SELECT rt.hotel_id, rt.room_type_id, rt.room_name, MIN(d.available_rooms) AS available_rooms,
                   rt.max_guests_per_room, rt.price_per_night_cents, rt.currency
            FROM inventory_room_types rt
            JOIN inventory_days d ON d.room_type_id = rt.room_type_id
            WHERE d.inventory_date >= ? AND d.inventory_date < ? AND rt.max_guests_per_room >= ?
            GROUP BY rt.hotel_id, rt.room_type_id, rt.room_name, rt.max_guests_per_room,
                     rt.price_per_night_cents, rt.currency
            HAVING COUNT(d.inventory_date) = ?
            ORDER BY rt.price_per_night_cents, rt.room_name
            """;
    private static final String AVAILABILITY_BY_ID_SQL = """
            SELECT rt.hotel_id, rt.room_type_id, rt.room_name, MIN(d.available_rooms) AS available_rooms,
                   rt.max_guests_per_room, rt.price_per_night_cents, rt.currency
            FROM inventory_room_types rt
            JOIN inventory_days d ON d.room_type_id = rt.room_type_id
            WHERE d.inventory_date >= ? AND d.inventory_date < ? AND rt.room_type_id = ?
            GROUP BY rt.hotel_id, rt.room_type_id, rt.room_name, rt.max_guests_per_room,
                     rt.price_per_night_cents, rt.currency
            HAVING COUNT(d.inventory_date) = ?
            """;
    private static final RowMapper<RoomAvailability> OFFER_MAPPER = (row, index) -> new RoomAvailability(
            row.getString("hotel_id"), row.getString("room_type_id"), row.getString("room_name"),
            row.getInt("available_rooms"), row.getInt("max_guests_per_room"),
            row.getLong("price_per_night_cents"), row.getString("currency").trim());

    private final JdbcTemplate jdbc;

    public JdbcRoomInventory(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<RoomAvailability> search(StayPeriod stay, int guests) {
        return jdbc.query(AVAILABILITY_SQL, OFFER_MAPPER, stay.checkIn(), stay.checkOut(), guests, stay.nights());
    }

    @Override
    public Optional<RoomAvailability> findAvailability(String roomTypeId, StayPeriod stay) {
        List<RoomAvailability> offers = jdbc.query(AVAILABILITY_BY_ID_SQL, OFFER_MAPPER,
                stay.checkIn(), stay.checkOut(), roomTypeId, stay.nights());
        return offers.stream().findFirst();
    }

    @Override
    public void reserve(String roomTypeId, StayPeriod stay, int rooms) {
        List<InventoryDay> days = lockDays(roomTypeId, stay);
        if (days.size() != stay.nights() || days.stream().anyMatch(day -> day.availableRooms() < rooms)) {
            throw new RoomUnavailableException(roomTypeId);
        }
        for (InventoryDay day : days) {
            int updated = jdbc.update("""
                    UPDATE inventory_days SET available_rooms = available_rooms - ?
                    WHERE room_type_id = ? AND inventory_date = ? AND available_rooms >= ?
                    """, rooms, roomTypeId, day.date(), rooms);
            if (updated != 1) {
                throw new RoomUnavailableException(roomTypeId);
            }
        }
    }

    @Override
    public void release(String roomTypeId, StayPeriod stay, int rooms) {
        List<InventoryDay> days = lockDays(roomTypeId, stay);
        if (days.size() != stay.nights()) {
            throw new RoomUnavailableException(roomTypeId);
        }
        for (InventoryDay day : days) {
            jdbc.update("""
                    UPDATE inventory_days
                    SET available_rooms = LEAST(total_rooms, available_rooms + ?)
                    WHERE room_type_id = ? AND inventory_date = ?
                    """, rooms, roomTypeId, day.date());
        }
    }

    private List<InventoryDay> lockDays(String roomTypeId, StayPeriod stay) {
        return jdbc.query("""
                SELECT inventory_date, available_rooms FROM inventory_days
                WHERE room_type_id = ? AND inventory_date >= ? AND inventory_date < ?
                ORDER BY inventory_date FOR UPDATE
                """, (row, index) -> new InventoryDay(row.getObject("inventory_date", LocalDate.class),
                row.getInt("available_rooms")), roomTypeId, stay.checkIn(), stay.checkOut());
    }

    private record InventoryDay(java.time.LocalDate date, int availableRooms) {
    }
}
