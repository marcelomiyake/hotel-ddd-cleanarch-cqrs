import type { HotelReadPort, ReservationCommandPort, ReservationReadPort } from "../application/ports";
import type { HotelCard, HotelDetails, RoomAvailability } from "../domain/hotel";
import type { PlaceReservationInput, Reservation } from "../domain/reservation";
import type { SearchCriteria } from "../domain/stay";

async function requestJson<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: { Accept: "application/json", ...init?.headers },
  });
  if (!response.ok) {
    let message = `Request failed (${response.status}).`;
    try {
      const problem = await response.json() as { detail?: string; title?: string };
      message = problem.detail ?? problem.title ?? message;
    } catch {
      message = response.statusText || message;
    }
    throw new Error(message);
  }
  return response.json() as Promise<T>;
}

export class HotelHttpGateway implements HotelReadPort {
  searchHotels(city: string): Promise<HotelCard[]> {
    return requestJson(`/api/catalog/hotels?city=${encodeURIComponent(city)}`);
  }

  getHotel(hotelId: string): Promise<HotelDetails> {
    return requestJson(`/api/catalog/hotels/${encodeURIComponent(hotelId)}`);
  }
}

export class ReservationHttpGateway implements ReservationReadPort, ReservationCommandPort {
  getAvailability(stay: SearchCriteria): Promise<RoomAvailability[]> {
    const query = new URLSearchParams({
      checkIn: stay.checkIn,
      checkOut: stay.checkOut,
      guests: String(stay.guests),
    });
    return requestJson(`/api/booking/availability?${query.toString()}`);
  }

  findReservations(email: string): Promise<Reservation[]> {
    return requestJson(`/api/booking/reservations?email=${encodeURIComponent(email)}`);
  }

  placeReservation(input: PlaceReservationInput, idempotencyKey: string): Promise<Reservation> {
    return requestJson("/api/booking/reservations", {
      method: "POST",
      headers: { "Content-Type": "application/json", "Idempotency-Key": idempotencyKey },
      body: JSON.stringify(input),
    });
  }

  cancelReservation(id: string, email: string): Promise<Reservation> {
    return requestJson(`/api/booking/reservations/${encodeURIComponent(id)}?email=${encodeURIComponent(email)}`, {
      method: "DELETE",
    });
  }
}
