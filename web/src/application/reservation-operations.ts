import type { ReservationCommandPort, ReservationReadPort } from "./ports";
import type { PlaceReservationInput, Reservation } from "../domain/reservation";
import { normalizeEmail, validGuest } from "../domain/reservation";

export class PlaceReservationCommand {
  constructor(private readonly reservations: ReservationCommandPort) {}

  execute(input: PlaceReservationInput, idempotencyKey: string): Promise<Reservation> {
    if (!validGuest(input.guestEmail, input.guestName)) {
      return Promise.reject(new Error("Enter your name and a valid email address."));
    }
    if (!input.hotelId || !input.roomTypeId || !idempotencyKey.trim()) {
      return Promise.reject(new Error("Choose a room before making a reservation."));
    }
    return this.reservations.placeReservation({
      ...input,
      guestName: input.guestName.trim(),
      guestEmail: normalizeEmail(input.guestEmail),
    }, idempotencyKey.trim());
  }
}

export class FindReservationsQuery {
  constructor(private readonly reservations: ReservationReadPort) {}

  execute(email: string): Promise<Reservation[]> {
    const normalized = normalizeEmail(email);
    if (!validGuest(normalized, "Guest")) {
      return Promise.reject(new Error("Enter the email used for your reservation."));
    }
    return this.reservations.findReservations(normalized);
  }
}

export class CancelReservationCommand {
  constructor(private readonly reservations: ReservationCommandPort) {}

  execute(id: string, email: string): Promise<Reservation> {
    if (!id.trim() || !validGuest(email, "Guest")) {
      return Promise.reject(new Error("Check the reservation and guest email before cancelling."));
    }
    return this.reservations.cancelReservation(id.trim(), normalizeEmail(email));
  }
}
