import type { HotelCard, RoomAvailability } from "../domain/hotel";
import type { SearchCriteria } from "../domain/stay";
import { formatMoney, localDateAtOffset } from "../domain/stay";

type SearchFormProps = {
  readonly criteria: SearchCriteria;
  readonly onChange: (next: SearchCriteria) => void;
  readonly onSearch: () => void;
  readonly busy?: boolean;
  readonly compact?: boolean;
};

export function SearchForm({ criteria, onChange, onSearch, busy = false, compact = false }: SearchFormProps) {
  const update = (field: keyof SearchCriteria, value: string | number) => {
    onChange({ ...criteria, [field]: value });
  };

  return (
    <form
      className={compact ? "search-form compact" : "search-form"}
      aria-label="Search stays"
      onSubmit={(event) => { event.preventDefault(); onSearch(); }}
    >
      <div className="search-field destination-field">
        <label htmlFor="destination">Where to?</label>
        <span className="field-value-wrap">
          <span className="field-icon" aria-hidden="true">⌖</span>
          <input id="destination" name="destination" list="destinations" value={criteria.city}
            onChange={(event) => update("city", event.target.value)} placeholder="City or neighborhood"
            autoComplete="address-level2" required />
          <datalist id="destinations"><option value="Rio de Janeiro" /><option value="São Paulo" /></datalist>
        </span>
      </div>
      <div className="search-field date-field">
        <label htmlFor="check-in">Check in</label>
        <input id="check-in" name="check-in" type="date" min={localDateAtOffset(0)} value={criteria.checkIn}
          onChange={(event) => update("checkIn", event.target.value)} required />
      </div>
      <div className="search-field date-field">
        <label htmlFor="check-out">Check out</label>
        <input id="check-out" name="check-out" type="date" min={criteria.checkIn} value={criteria.checkOut}
          onChange={(event) => update("checkOut", event.target.value)} required />
      </div>
      <div className="search-field guests-field">
        <label htmlFor="guests">Guests</label>
        <select id="guests" name="guests" value={criteria.guests}
          onChange={(event) => update("guests", Number(event.target.value))}>
          {Array.from({ length: 8 }, (_, index) => index + 1).map((count) => (
            <option value={count} key={count}>{count} {count === 1 ? "guest" : "guests"}</option>
          ))}
        </select>
      </div>
      <div className="search-field rooms-field">
        <label htmlFor="rooms">Rooms</label>
        <select id="rooms" name="rooms" value={criteria.rooms}
          onChange={(event) => update("rooms", Number(event.target.value))}>
          {[1, 2, 3].map((count) => <option value={count} key={count}>{count} {count === 1 ? "room" : "rooms"}</option>)}
        </select>
      </div>
      <button className="button button-primary search-button" type="submit" disabled={busy}>
        <span aria-hidden="true">⌕</span>{busy ? "Searching" : "Search stays"}
      </button>
    </form>
  );
}

type HotelCardProps = {
  readonly hotel: HotelCard;
  readonly availability: RoomAvailability[];
  readonly onSelect: (hotel: HotelCard) => void;
};

export function HotelCardView({ hotel, availability, onSelect }: HotelCardProps) {
  const rooms = availability.filter((offer) => offer.hotelId === hotel.id && offer.availableRooms > 0);
  const lowestPrice = rooms.length > 0 ? Math.min(...rooms.map((offer) => offer.pricePerNightCents)) : hotel.priceFromCents;
  const price = formatMoney(lowestPrice, hotel.currency);

  return (
    <article className="hotel-card">
      <button className="hotel-card-image" type="button" onClick={() => onSelect(hotel)}
        aria-label={hotel.featured ? `View ${hotel.name}, Wayfarer edit` : `View ${hotel.name}`}>
        <img src={cardImageUrl(hotel.imageUrl)} srcSet={`${cardImageUrl(hotel.imageUrl)} 480w, ${hotel.imageUrl} 760w`}
          sizes="(max-width: 700px) 100vw, (max-width: 1200px) 50vw, 360px"
          alt={`${hotel.name}, ${hotel.city}`} width="760" height="540" loading="lazy" decoding="async" />
        {hotel.featured && <span className="image-badge">Wayfarer edit</span>}
        <span className="favorite-mark" aria-hidden="true">♡</span>
      </button>
      <div className="hotel-card-content">
        <div className="hotel-card-overline">
          <span>{hotel.city} <span aria-hidden="true">·</span> {hotel.starRating} stars</span>
          <span className="rating-pill"><span aria-hidden="true">★</span> {hotel.guestRating.toFixed(1)}</span>
        </div>
        <h3>{hotel.name}</h3>
        <p className="hotel-tagline">{hotel.tagline}</p>
        <ul className="highlight-list" aria-label="Hotel highlights">
          {hotel.highlights.slice(0, 3).map((highlight) => <li key={highlight}>{highlight}</li>)}
        </ul>
        <div className="hotel-card-bottom">
          <p><strong>{price}</strong><span> / night</span></p>
          <button className="text-link" type="button" onClick={() => onSelect(hotel)}>View stay <span aria-hidden="true">↗</span></button>
        </div>
        {rooms.length > 0 && <p className="availability-note">{rooms[0]?.availableRooms} rooms available for your dates</p>}
      </div>
    </article>
  );
}

function cardImageUrl(imageUrl: string): string {
  return imageUrl.replace(/\.webp$/, "-card.webp");
}
