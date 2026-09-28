package com.wayfarer.reservation.adapter.out.postgres;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Component
public class InventoryBootstrap implements ApplicationRunner {
    private static final List<RoomTypeSeed> ROOM_TYPES = List.of(
            new RoomTypeSeed("room-casa-mar-terrace", "rio-casa-do-mar", "Terrace King", 2, 148000, 7),
            new RoomTypeSeed("room-casa-mar-ocean", "rio-casa-do-mar", "Ocean Suite", 3, 212000, 4),
            new RoomTypeSeed("room-santa-garden", "rio-santa-teresa", "Garden Room", 2, 126000, 8),
            new RoomTypeSeed("room-santa-view", "rio-santa-teresa", "City View Suite", 3, 186000, 4),
            new RoomTypeSeed("room-copa-balcony", "rio-copacabana-house", "Balcony Queen", 2, 99000, 12),
            new RoomTypeSeed("room-copa-family", "rio-copacabana-house", "Family Room", 4, 139000, 6),
            new RoomTypeSeed("room-jardim-suite", "rio-jardim-botanico", "Courtyard Suite", 2, 118000, 8),
            new RoomTypeSeed("room-jardim-family", "rio-jardim-botanico", "Family Residence", 4, 167000, 4),
            new RoomTypeSeed("room-jardins-studio", "sp-jardins-residence", "Jardins Studio", 2, 132000, 9),
            new RoomTypeSeed("room-jardins-suite", "sp-jardins-residence", "Freire Suite", 3, 198000, 4),
            new RoomTypeSeed("room-vila-courtyard", "sp-vila-madalena", "Courtyard Queen", 2, 87000, 9),
            new RoomTypeSeed("room-vila-family", "sp-vila-madalena", "Artist Loft", 4, 124000, 4));

    private final JdbcTemplate jdbc;
    private final Clock clock;

    public InventoryBootstrap(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        LocalDate firstDate = LocalDate.now(clock);
        for (RoomTypeSeed room : ROOM_TYPES) {
            seedRoomType(room);
            seedDates(room, firstDate);
        }
    }

    private void seedRoomType(RoomTypeSeed room) {
        jdbc.update("""
                INSERT INTO inventory_room_types
                    (room_type_id, hotel_id, room_name, max_guests_per_room, price_per_night_cents, currency, total_rooms)
                VALUES (?, ?, ?, ?, ?, 'BRL', ?)
                ON CONFLICT (room_type_id) DO NOTHING
                """, room.id(), room.hotelId(), room.name(), room.maxGuests(), room.priceCents(), room.totalRooms());
    }

    private void seedDates(RoomTypeSeed room, LocalDate firstDate) {
        jdbc.update("""
                INSERT INTO inventory_days (room_type_id, inventory_date, total_rooms, available_rooms)
                SELECT ?, day::date, ?, ?
                FROM generate_series(?::date, (?::date + INTERVAL '400 days'), INTERVAL '1 day') AS day
                ON CONFLICT (room_type_id, inventory_date) DO NOTHING
                """, room.id(), room.totalRooms(), room.totalRooms(), firstDate, firstDate);
    }

    private record RoomTypeSeed(String id, String hotelId, String name, int maxGuests,
                                long priceCents, int totalRooms) {
    }
}
