INSERT INTO hotels (id, name, city, country, address, stars, guest_rating, review_count, currency,
                    tagline, description, location_summary, hero_image_url, gallery_urls, highlights, featured)
VALUES
('rio-casa-do-mar', 'Casa do Mar', 'Rio de Janeiro', 'Brazil', 'Av. Vieira Souto, 170 · Ipanema', 5, 9.6, 842, 'BRL',
 'A quiet corner of Ipanema, made for slow mornings.', 'Wake to the Atlantic, wander the quiet end of Ipanema, then return to an intimate hotel shaped by Brazilian craft and warm hospitality.', 'Ipanema · 2 min walk to the beach', '/hotels/casa-mar.webp', '/hotels/casa-mar.webp|/hotels/casa-mar-room.webp|/hotels/casa-mar-pool.webp', 'Ocean view|Breakfast included|Rooftop pool', TRUE),
('rio-santa-teresa', 'Santa Teresa Atelier', 'Rio de Janeiro', 'Brazil', 'Rua Almirante Alexandrino, 660 · Santa Teresa', 5, 9.4, 516, 'BRL',
 'A hillside hideaway in Rio’s most storied neighborhood.', 'An intimate collection of rooms, a garden pool and sweeping views across the city, tucked between galleries and the tram lines of Santa Teresa.', 'Santa Teresa · Garden district', '/hotels/santa-teresa.webp', '/hotels/santa-teresa.webp|/hotels/santa-room.webp|/hotels/santa-pool.webp', 'Garden pool|Art collection|Breakfast included', TRUE),
('rio-copacabana-house', 'Copacabana House', 'Rio de Janeiro', 'Brazil', 'Av. Atlântica, 980 · Copacabana', 4, 9.1, 1208, 'BRL',
 'The easy rhythm of the promenade, right outside.', 'A bright, design-led base on the Copacabana promenade with a generous breakfast, a calm rooftop and all of the city within easy reach.', 'Copacabana · Beachfront', '/hotels/copacabana.webp', '/hotels/copacabana.webp|/hotels/copa-room.webp|/hotels/copa-rooftop.webp', 'Beachfront|Rooftop bar|Family rooms', FALSE),
('rio-jardim-botanico', 'Jardim Botanico Lodge', 'Rio de Janeiro', 'Brazil', 'Rua Pacheco Leão, 105 · Jardim Botânico', 4, 9.3, 347, 'BRL',
 'A leafy retreat at the foot of the mountain.', 'A warm, residential retreat beside the Botanical Garden with a considered restaurant, shaded courtyards and generous suites for longer stays.', 'Jardim Botânico · Near the gardens', '/hotels/jardim.webp', '/hotels/jardim.webp|/hotels/jardim-room.webp|/hotels/jardim-courtyard.webp', 'Courtyard garden|Restaurant|Family rooms', FALSE),
('sp-jardins-residence', 'Jardins Residence', 'São Paulo', 'Brazil', 'Rua Oscar Freire, 530 · Jardins', 5, 9.5, 691, 'BRL',
 'A thoughtful city stay on São Paulo’s best-loved street.', 'Contemporary Brazilian design meets apartment-like comfort in Jardins, moments from independent shops, galleries and excellent restaurants.', 'Jardins · Walkable neighborhood', '/hotels/jardins.webp', '/hotels/jardins.webp|/hotels/jardins-room.webp|/hotels/jardins-lounge.webp', 'Design district|Fitness studio|Breakfast included', FALSE),
('sp-vila-madalena', 'Vila Madalena House', 'São Paulo', 'Brazil', 'Rua Harmonia, 340 · Vila Madalena', 4, 9.2, 423, 'BRL',
 'Color, good coffee and a slower side of the city.', 'An independent neighborhood hotel surrounded by studios, street art and cafes, with a lively courtyard and a warm, local point of view.', 'Vila Madalena · Gallery district', '/hotels/vila.webp', '/hotels/vila.webp|/hotels/vila-room.webp|/hotels/vila-courtyard.webp', 'Courtyard|Neighborhood guide|Pet friendly', FALSE);

INSERT INTO room_types (id, hotel_id, name, description, bed_summary, max_guests, price_per_night_cents, currency, amenities)
VALUES
('room-casa-mar-terrace', 'rio-casa-do-mar', 'Terrace King', 'A light-filled room with a private terrace and a glimpse of the ocean.', '1 king bed', 2, 148000, 'BRL', 'Ocean glimpse|Private terrace|Rain shower|Breakfast'),
('room-casa-mar-ocean', 'rio-casa-do-mar', 'Ocean Suite', 'A generous suite with a wide Atlantic view and a separate sitting area.', '1 king bed + sofa', 3, 212000, 'BRL', 'Ocean view|Separate lounge|Rain shower|Breakfast'),
('room-santa-garden', 'rio-santa-teresa', 'Garden Room', 'A quiet room opening onto the courtyard garden.', '1 queen bed', 2, 126000, 'BRL', 'Garden access|Rain shower|Breakfast'),
('room-santa-view', 'rio-santa-teresa', 'City View Suite', 'A hillside suite with room to unwind and a view over the city.', '1 king bed + sofa', 3, 186000, 'BRL', 'City view|Separate lounge|Breakfast'),
('room-copa-balcony', 'rio-copacabana-house', 'Balcony Queen', 'A bright room with a furnished balcony over the promenade.', '1 queen bed', 2, 99000, 'BRL', 'Balcony|Beach access|Breakfast'),
('room-copa-family', 'rio-copacabana-house', 'Family Room', 'A flexible layout for a small family, close to the rooftop pool.', '1 king bed + bunk beds', 4, 139000, 'BRL', 'Family layout|Rooftop pool|Breakfast'),
('room-jardim-suite', 'rio-jardim-botanico', 'Courtyard Suite', 'A peaceful suite beside the leafy courtyard.', '1 king bed', 2, 118000, 'BRL', 'Courtyard|Rain shower|Breakfast'),
('room-jardim-family', 'rio-jardim-botanico', 'Family Residence', 'An expansive residence with a small kitchenette and lounge.', '1 king bed + sofa', 4, 167000, 'BRL', 'Kitchenette|Living area|Breakfast'),
('room-jardins-studio', 'sp-jardins-residence', 'Jardins Studio', 'A generous studio designed with Brazilian woods and linen.', '1 queen bed', 2, 132000, 'BRL', 'Work desk|Rain shower|Breakfast'),
('room-jardins-suite', 'sp-jardins-residence', 'Freire Suite', 'A spacious city suite with a separate living area.', '1 king bed + sofa', 3, 198000, 'BRL', 'Separate lounge|Work desk|Breakfast'),
('room-vila-courtyard', 'sp-vila-madalena', 'Courtyard Queen', 'A quiet courtyard room close to the neighborhood’s cafes.', '1 queen bed', 2, 87000, 'BRL', 'Courtyard|Neighborhood guide|Breakfast'),
('room-vila-family', 'sp-vila-madalena', 'Artist Loft', 'A two-level loft with room for friends or family.', '1 king bed + 2 singles', 4, 124000, 'BRL', 'Loft layout|Courtyard|Breakfast');
