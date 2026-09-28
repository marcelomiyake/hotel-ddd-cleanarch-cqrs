import type { RoomAvailability, RoomType } from "../domain/hotel";
import { formatMoney } from "../domain/stay";

type RoomCardProps = {
  readonly room: RoomType;
  readonly availability?: RoomAvailability;
  readonly selected: boolean;
  readonly onSelect: (room: RoomType) => void;
};

export function RoomCardView({ room, availability, selected, onSelect }: RoomCardProps) {
  const availableRooms = availability?.availableRooms ?? 0;
  const price = availability?.pricePerNightCents ?? room.pricePerNightCents;
  let actionLabel = "Unavailable";
  if (selected) actionLabel = "Selected";
  else if (availableRooms > 0) actionLabel = "Choose room";
  return (
    <article className={selected ? "room-card selected" : "room-card"}>
      <div className="room-card-copy">
        <div className="room-title-line">
          <div><p className="eyebrow">{room.bedSummary}</p><h3>{room.name}</h3></div>
          <p className="room-capacity"><span aria-hidden="true">♙</span> Up to {room.maxGuests} guests</p>
        </div>
        <p className="room-description">{room.description}</p>
        <ul className="amenity-list">{room.amenities.map((amenity) => <li key={amenity}>{amenity}</li>)}</ul>
        <p className={availableRooms > 0 ? "stock-note" : "stock-note sold-out"}>
          {availableRooms > 0 ? `${availableRooms} available for your dates` : "Check availability for this room"}
        </p>
      </div>
      <div className="room-card-price">
        <p><strong>{formatMoney(price, room.currency)}</strong><span> / night</span></p>
        <button className={selected ? "button button-secondary" : "button button-primary"} type="button"
          disabled={availableRooms < 1} onClick={() => onSelect(room)}>
          {actionLabel}
        </button>
      </div>
    </article>
  );
}
