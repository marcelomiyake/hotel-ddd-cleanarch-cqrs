import { useState } from "react";
import type { HotelCard, HotelDetails, RoomAvailability, RoomType } from "../domain/hotel";
import type { Reservation } from "../domain/reservation";
import type { SearchCriteria } from "../domain/stay";
import { formatMoney, formatStay, localDateAtOffset } from "../domain/stay";
import { HotelCardView, SearchForm } from "./home-components";
import { RoomCardView } from "./room-component";

type SearchResultsPageProps = {
  readonly criteria: SearchCriteria;
  readonly onCriteriaChange: (next: SearchCriteria) => void;
  readonly onSearch: () => void;
  readonly onSelectHotel: (hotel: HotelCard) => void;
  readonly hotels: HotelCard[];
  readonly availability: RoomAvailability[];
  readonly busy: boolean;
};

export function SearchResultsPage(props: SearchResultsPageProps) {
  return (
    <section className="page-width results-page" aria-labelledby="results-title">
      <div className="results-search"><SearchForm criteria={props.criteria} onChange={props.onCriteriaChange}
        onSearch={props.onSearch} busy={props.busy} compact /></div>
      <div className="results-heading">
        <div>
          <p className="eyebrow">Your next chapter</p>
          <h1 id="results-title">Stays in {props.criteria.city}</h1>
          <p className="results-meta">{formatStay(props.criteria.checkIn, props.criteria.checkOut)} <span aria-hidden="true">·</span> {props.criteria.guests} guests <span aria-hidden="true">·</span> {props.hotels.length} considered stays</p>
        </div>
        <p className="results-edit">Showing stays with room for {props.criteria.guests}</p>
      </div>
      {props.hotels.length > 0 ? (
        <div className="hotel-grid results-grid">
          {props.hotels.map((hotel) => <HotelCardView key={hotel.id} hotel={hotel} availability={props.availability} onSelect={props.onSelectHotel} />)}
        </div>
      ) : (
        <div className="empty-state">
          <span className="empty-mark" aria-hidden="true">⌂</span>
          <h2>No stays found for those dates.</h2>
          <p>Try a nearby date or a different destination. We’ll keep looking.</p>
          <button className="button button-secondary" type="button" onClick={props.onSearch}>Search again</button>
        </div>
      )}
    </section>
  );
}

type HotelDetailsPageProps = {
  readonly details: HotelDetails;
  readonly criteria: SearchCriteria;
  readonly availability: RoomAvailability[];
  readonly selectedRoomId: string;
  readonly onSelectRoom: (room: RoomType) => void;
  readonly onBack: () => void;
};

export function HotelDetailsPage({ details, criteria, availability, selectedRoomId, onSelectRoom, onBack }: HotelDetailsPageProps) {
  const photos = details.gallery.length > 0 ? details.gallery : [details.hotel.imageUrl];
  return (
    <section className="page-width details-page" aria-labelledby="hotel-title">
      <button className="back-link" type="button" onClick={onBack}><span aria-hidden="true">←</span> Back to stays</button>
      <div className="details-title-row">
        <div>
          <p className="eyebrow">{details.hotel.city} <span aria-hidden="true">·</span> {details.hotel.starRating} stars</p>
          <h1 id="hotel-title">{details.hotel.name}</h1>
          <p className="details-address">{details.hotel.address}</p>
        </div>
        <div className="details-score"><span aria-hidden="true">★</span><strong>{details.hotel.guestRating.toFixed(1)}</strong><span> from {details.hotel.reviewCount.toLocaleString("en-BR")} stays</span></div>
      </div>
      <div className="gallery-grid" aria-label={`${details.hotel.name} photo gallery`}>
        {photos.slice(0, 3).map((image, index) => (
          <img key={`${image}-${index}`} src={image} alt={`${details.hotel.name} view ${index + 1}`} width="900" height="620" loading={index === 0 ? "eager" : "lazy"} decoding="async" />
        ))}
      </div>
      <div className="details-layout">
        <div className="details-main">
          <div className="details-summary">
            <div><p className="eyebrow">The feeling</p><p className="details-tagline">{details.hotel.tagline}</p></div>
            <p>{details.description}</p>
          </div>
          <div className="details-section-heading">
            <div><p className="eyebrow">Stay your way</p><h2>Choose your room</h2></div>
            <p>{formatStay(criteria.checkIn, criteria.checkOut)} <span aria-hidden="true">·</span> {criteria.guests} guests</p>
          </div>
          <div className="room-list">
            {details.rooms.map((room) => (
              <RoomCardView key={room.id} room={room}
                availability={availability.find((offer) => offer.roomTypeId === room.id)}
                selected={selectedRoomId === room.id} onSelect={onSelectRoom} />
            ))}
          </div>
        </div>
        <aside className="location-card" aria-label="Location details">
          <p className="eyebrow">The neighborhood</p>
          <div className="location-art" aria-hidden="true"><span>⌖</span><i /><b /><em /></div>
          <h3>{details.locationSummary}</h3>
          <p>Good coffee, local tables and a little more time outside are all close by.</p>
          <p className="location-detail">{details.hotel.address}</p>
        </aside>
      </div>
    </section>
  );
}

type CheckoutPageProps = {
  readonly details: HotelDetails;
  readonly room: RoomType;
  readonly criteria: SearchCriteria;
  readonly guestName: string;
  readonly guestEmail: string;
  readonly error: string;
  readonly busy: boolean;
  readonly onGuestName: (name: string) => void;
  readonly onGuestEmail: (email: string) => void;
  readonly onSubmit: () => void;
  readonly onBack: () => void;
};

export function CheckoutPage(props: CheckoutPageProps) {
  const offer = props.room;
  const subtotal = offer.pricePerNightCents * Math.round((Date.parse(`${props.criteria.checkOut}T00:00:00Z`) - Date.parse(`${props.criteria.checkIn}T00:00:00Z`)) / 86_400_000) * props.criteria.rooms;
  return (
    <section className="page-width checkout-page" aria-labelledby="checkout-title">
      <button className="back-link" type="button" onClick={props.onBack}><span aria-hidden="true">←</span> Change room</button>
      <div className="checkout-heading"><p className="eyebrow">One last detail</p><h1 id="checkout-title">Make it yours</h1><p>Your stay is held while you complete the details below.</p></div>
      <div className="checkout-layout">
        <form className="guest-form" onSubmit={(event) => { event.preventDefault(); props.onSubmit(); }}>
          <div className="form-section-title"><span>01</span><div><h2>Who’s coming?</h2><p>We’ll send your confirmation here.</p></div></div>
          <label className="input-label" htmlFor="guest-name">Full name</label>
          <input className="text-input" id="guest-name" name="guest-name" autoComplete="name" value={props.guestName}
            onChange={(event) => props.onGuestName(event.target.value)} placeholder="Your name" required maxLength={120} />
          <label className="input-label" htmlFor="guest-email">Email address</label>
          <input className="text-input" id="guest-email" name="guest-email" type="email" autoComplete="email" value={props.guestEmail}
            onChange={(event) => props.onGuestEmail(event.target.value)} placeholder="you@example.com" required maxLength={254} />
          <div className="pay-at-property"><span className="pay-icon" aria-hidden="true">✓</span><div><strong>Pay at the property</strong><p>No card is needed to reserve this stay.</p></div></div>
          {props.error && <p className="form-error" role="alert">{props.error}</p>}
          <button className="button button-primary confirm-button" type="submit" disabled={props.busy}>
            {props.busy ? "Reserving your stay…" : "Confirm reservation"}<span aria-hidden="true">↗</span>
          </button>
          <p className="terms-note">By confirming, you agree to the hotel’s cancellation policy. You can cancel free of charge before check-in day.</p>
        </form>
        <aside className="booking-summary" aria-label="Reservation summary">
          <img src={props.details.hotel.imageUrl} alt="" width="760" height="480" />
          <div className="booking-summary-inner">
            <p className="eyebrow">Your stay</p><h2>{props.details.hotel.name}</h2><p>{offer.name} <span aria-hidden="true">·</span> {offer.bedSummary}</p>
            <div className="summary-row"><span>Dates</span><strong>{formatStay(props.criteria.checkIn, props.criteria.checkOut)}</strong></div>
            <div className="summary-row"><span>Guests</span><strong>{props.criteria.guests} guests <span aria-hidden="true">·</span> {props.criteria.rooms} {props.criteria.rooms === 1 ? "room" : "rooms"}</strong></div>
            <div className="summary-row"><span>{formatMoney(offer.pricePerNightCents, offer.currency)} × {Math.round((Date.parse(`${props.criteria.checkOut}T00:00:00Z`) - Date.parse(`${props.criteria.checkIn}T00:00:00Z`)) / 86_400_000)} nights</span><strong>{formatMoney(subtotal, offer.currency)}</strong></div>
            <div className="summary-total"><span>Total due at the property</span><strong>{formatMoney(subtotal, offer.currency)}</strong></div>
            <p className="summary-policy">Taxes included <span aria-hidden="true">·</span> Free cancellation before check-in</p>
          </div>
        </aside>
      </div>
    </section>
  );
}

type ConfirmationPageProps = {
  readonly reservation: Reservation;
  readonly hotel?: HotelCard;
  readonly onHome: () => void;
  readonly onTrips: () => void;
};

export function ConfirmationPage({ reservation, hotel, onHome, onTrips }: ConfirmationPageProps) {
  return (
    <section className="page-width confirmation-page" aria-labelledby="confirmation-title">
      <div className="confirmation-mark" aria-hidden="true">✓</div>
      <p className="eyebrow">All set</p>
      <h1 id="confirmation-title">Your time away<br />is taking shape<span className="brand-period">.</span></h1>
      <p className="confirmation-copy">A confirmation is ready for <strong>{reservation.guestEmail}</strong>. We’re looking forward to having you.</p>
      <article className="confirmation-card">
        <div className="confirmation-card-heading"><div><p className="eyebrow">Reservation code</p><h2>{reservation.id.slice(0, 8).toUpperCase()}</h2></div><span className="status-badge">Confirmed</span></div>
        <div className="confirmation-divider" />
        <div className="confirmation-stay"><img src={hotel?.imageUrl ?? "/hotels/hero.webp"} alt="" width="480" height="360" /><div><h3>{hotel?.name ?? reservation.hotelId}</h3><p>{reservation.roomName}</p><p>{formatStay(reservation.checkIn, reservation.checkOut)} <span aria-hidden="true">·</span> {reservation.nights} nights</p><strong>{formatMoney(reservation.totalPriceCents, reservation.currency)}</strong><span> due at the property</span></div></div>
      </article>
      <div className="confirmation-actions"><button className="button button-primary" type="button" onClick={onTrips}>View my trips</button><button className="button button-secondary" type="button" onClick={onHome}>Find another stay</button></div>
    </section>
  );
}

type TripsPageProps = {
  readonly hotels: HotelCard[];
  readonly reservations: Reservation[];
  readonly initialEmail: string;
  readonly busy: boolean;
  readonly error: string;
  readonly onFind: (email: string) => void;
  readonly onCancel: (reservation: Reservation) => void;
};

export function TripsPage({ hotels, reservations, initialEmail, busy, error, onFind, onCancel }: TripsPageProps) {
  const [email, setEmail] = useState(initialEmail);
  return (
    <section className="page-width trips-page" aria-labelledby="trips-title">
      <div className="trips-heading"><p className="eyebrow">Your Wayfarer</p><h1 id="trips-title">The good part is<br />on the way<span className="brand-period">.</span></h1><p>Look up a booking or make a little room for what’s next.</p></div>
      <form className="trip-lookup" onSubmit={(event) => { event.preventDefault(); onFind(email); }}>
        <label htmlFor="trip-email">Email used for your reservation</label>
        <div className="trip-lookup-row"><input className="text-input" id="trip-email" type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
          <button className="button button-primary" type="submit" disabled={busy}>{busy ? "Looking…" : "Find my trips"}</button></div>
        {error && <p className="form-error" role="alert">{error}</p>}
      </form>
      {reservations.length > 0 ? (
        <div className="trip-list" aria-label="Your reservations">
          {reservations.map((reservation) => {
            const hotel = hotels.find((item) => item.id === reservation.hotelId);
            const cancellable = reservation.status === "CONFIRMED" && reservation.checkIn > localDateAtOffset(0);
            return (
              <article className="trip-card" key={reservation.id}>
                <img src={hotel?.imageUrl ?? "/hotels/hero.webp"} alt="" width="520" height="400" loading="lazy" />
                <div className="trip-card-info"><div className="trip-card-title"><div><p className="eyebrow">{hotel?.city ?? "Your stay"}</p><h2>{hotel?.name ?? reservation.hotelId}</h2></div><span className={reservation.status === "CONFIRMED" ? "status-badge" : "status-badge cancelled"}>{reservation.status === "CONFIRMED" ? "Confirmed" : "Cancelled"}</span></div>
                  <p>{reservation.roomName} <span aria-hidden="true">·</span> {reservation.guests} guests</p><p>{formatStay(reservation.checkIn, reservation.checkOut)} <span aria-hidden="true">·</span> {reservation.nights} nights</p>
                  <div className="trip-card-bottom"><strong>{formatMoney(reservation.totalPriceCents, reservation.currency)}</strong>{cancellable && <button className="text-link cancel-link" type="button" onClick={() => onCancel(reservation)}>Cancel reservation</button>}</div>
                </div>
              </article>
            );
          })}
        </div>
      ) : (
        <div className="trips-empty"><span aria-hidden="true">✳</span><div><h2>Your next stay starts here.</h2><p>Enter the email address used when you booked to find a reservation.</p></div></div>
      )}
    </section>
  );
}
