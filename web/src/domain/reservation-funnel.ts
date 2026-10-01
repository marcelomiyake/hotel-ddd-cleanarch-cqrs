export type ReservationFunnelEventType = "RESERVATION_STARTED" | "SCREEN_VIEWED" | "RESERVATION_CONFIRMED";

export type ReservationScreen = "home" | "results" | "hotel" | "checkout" | "confirmation" | "trips";

export type ReservationFunnelEvent = {
  readonly sessionId: string;
  readonly reservationAttemptId: string;
  readonly eventType: ReservationFunnelEventType;
  readonly screen: ReservationScreen;
  readonly hotelId?: string;
  readonly roomTypeId?: string;
};
