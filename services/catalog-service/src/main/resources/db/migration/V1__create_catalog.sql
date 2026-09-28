CREATE TABLE hotels (
    id VARCHAR(80) PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(80) NOT NULL,
    address VARCHAR(240) NOT NULL,
    stars SMALLINT NOT NULL CHECK (stars BETWEEN 1 AND 5),
    guest_rating NUMERIC(2, 1) NOT NULL CHECK (guest_rating BETWEEN 0 AND 10),
    review_count INTEGER NOT NULL CHECK (review_count >= 0),
    currency CHAR(3) NOT NULL,
    tagline VARCHAR(240) NOT NULL,
    description TEXT NOT NULL,
    location_summary VARCHAR(300) NOT NULL,
    hero_image_url VARCHAR(300) NOT NULL,
    gallery_urls TEXT NOT NULL,
    highlights TEXT NOT NULL,
    featured BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE room_types (
    id VARCHAR(100) PRIMARY KEY,
    hotel_id VARCHAR(80) NOT NULL REFERENCES hotels(id),
    name VARCHAR(140) NOT NULL,
    description TEXT NOT NULL,
    bed_summary VARCHAR(100) NOT NULL,
    max_guests SMALLINT NOT NULL CHECK (max_guests > 0),
    price_per_night_cents BIGINT NOT NULL CHECK (price_per_night_cents >= 0),
    currency CHAR(3) NOT NULL,
    amenities TEXT NOT NULL
);

CREATE INDEX hotels_city_rating_idx ON hotels (city, guest_rating DESC);
CREATE INDEX room_types_hotel_price_idx ON room_types (hotel_id, price_per_night_cents);
