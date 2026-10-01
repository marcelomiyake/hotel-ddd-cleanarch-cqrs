import { afterEach, describe, expect, it, vi } from "vitest";
import { ReservationHttpGateway } from "../../src/infrastructure/api-gateways";
import type { ReservationFunnelEvent } from "../../src/domain/reservation-funnel";

afterEach(() => vi.unstubAllGlobals());

describe("reservation funnel HTTP adapter", () => {
  const event: ReservationFunnelEvent = {
    sessionId: "16d4387b-8054-4c95-9bd2-a7349d503160",
    reservationAttemptId: "2d73e56b-69cf-47bd-8dca-3716546f2cdf",
    eventType: "RESERVATION_STARTED",
    screen: "checkout",
    hotelId: "hotel-1",
    roomTypeId: "room-1",
  };

  it("posts the event without adding guest information", async () => {
    const fetchMock = vi.fn().mockResolvedValue({ ok: true });
    vi.stubGlobal("fetch", fetchMock);

    await new ReservationHttpGateway().recordReservationFunnelEvent(event);

    expect(fetchMock).toHaveBeenCalledWith("/api/booking/reservation-funnel-events", {
      method: "POST",
      headers: { Accept: "application/json", "Content-Type": "application/json" },
      body: JSON.stringify(event),
    });
  });

  it("rejects a failed event write so the application recorder can swallow it", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({ ok: false, status: 503 }));

    await expect(new ReservationHttpGateway().recordReservationFunnelEvent(event))
      .rejects.toThrow("Request failed (503).");
  });
});
