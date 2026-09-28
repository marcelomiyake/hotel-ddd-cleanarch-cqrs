CREATE TABLE inventory_room_types (
    room_type_id VARCHAR(100) PRIMARY KEY,
    hotel_id VARCHAR(80) NOT NULL,
    room_name VARCHAR(140) NOT NULL,
    max_guests_per_room SMALLINT NOT NULL CHECK (max_guests_per_room > 0),
    price_per_night_cents BIGINT NOT NULL CHECK (price_per_night_cents >= 0),
    currency CHAR(3) NOT NULL,
    total_rooms SMALLINT NOT NULL CHECK (total_rooms > 0)
);

CREATE TABLE inventory_days (
    room_type_id VARCHAR(100) NOT NULL REFERENCES inventory_room_types(room_type_id),
    inventory_date DATE NOT NULL,
    total_rooms SMALLINT NOT NULL CHECK (total_rooms > 0),
    available_rooms SMALLINT NOT NULL CHECK (available_rooms >= 0 AND available_rooms <= total_rooms),
    PRIMARY KEY (room_type_id, inventory_date)
);

CREATE INDEX inventory_days_date_idx ON inventory_days (inventory_date, room_type_id);

CREATE TABLE reservations (
    id UUID PRIMARY KEY,
    idempotency_key VARCHAR(200) NOT NULL UNIQUE,
    hotel_id VARCHAR(80) NOT NULL,
    room_type_id VARCHAR(100) NOT NULL,
    room_name VARCHAR(140) NOT NULL,
    guest_name VARCHAR(120) NOT NULL,
    guest_email VARCHAR(254) NOT NULL,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    guest_count SMALLINT NOT NULL CHECK (guest_count > 0),
    room_count SMALLINT NOT NULL CHECK (room_count > 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('CONFIRMED', 'CANCELLED')),
    price_per_night_cents BIGINT NOT NULL CHECK (price_per_night_cents >= 0),
    total_price_cents BIGINT NOT NULL CHECK (total_price_cents >= 0),
    currency CHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX reservations_guest_email_created_idx ON reservations (guest_email, created_at DESC);
