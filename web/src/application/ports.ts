import type { HotelCard, HotelDetails, RoomAvailability } from "../domain/hotel";
import type { PlaceReservationInput, Reservation } from "../domain/reservation";
import type { ReservationFunnelEvent } from "../domain/reservation-funnel";
import type { SearchCriteria } from "../domain/stay";

export interface HotelReadPort {
  searchHotels(city: string): Promise<HotelCard[]>;
  getHotel(hotelId: string): Promise<HotelDetails>;
}

export interface ReservationReadPort {
  getAvailability(stay: SearchCriteria): Promise<RoomAvailability[]>;
  findReservations(email: string): Promise<Reservation[]>;
}

export interface ReservationCommandPort {
  placeReservation(input: PlaceReservationInput, idempotencyKey: string): Promise<Reservation>;
  cancelReservation(id: string, email: string): Promise<Reservation>;
}

export interface ReservationFunnelEventPort {
  recordReservationFunnelEvent(event: ReservationFunnelEvent): Promise<void>;
}
