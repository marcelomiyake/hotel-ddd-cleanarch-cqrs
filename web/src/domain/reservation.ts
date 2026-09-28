import type { SearchCriteria } from "./stay";

export type ReservationStatus = "CONFIRMED" | "CANCELLED";

export type Reservation = {
  id: string;
  hotelId: string;
  roomTypeId: string;
  roomName: string;
  guestName: string;
  guestEmail: string;
  checkIn: string;
  checkOut: string;
  nights: number;
  guests: number;
  rooms: number;
  status: ReservationStatus;
  pricePerNightCents: number;
  totalPriceCents: number;
  currency: string;
  createdAt: string;
};

export type PlaceReservationInput = SearchCriteria & {
  hotelId: string;
  roomTypeId: string;
  guestName: string;
  guestEmail: string;
};

export function normalizeEmail(email: string): string {
  return email.trim().toLowerCase();
}

export function validGuest(email: string, name: string): boolean {
  const normalized = normalizeEmail(email);
  const separator = normalized.indexOf("@");
  const domainDot = normalized.indexOf(".", separator + 1);
  return name.trim().length > 0
    && separator > 0
    && separator === normalized.lastIndexOf("@")
    && domainDot > separator + 1
    && domainDot < normalized.length - 1
    && !/\s/.test(normalized);
}
