import { describe, expect, it, vi } from "vitest";
import type { ReservationFunnelEventPort } from "../../src/application/ports";
import { getOrCreateReservationFunnelSessionId, ReservationFunnelEventRecorder } from "../../src/application/reservation-funnel-tracking";
import type { ReservationFunnelEvent } from "../../src/domain/reservation-funnel";

const SESSION_ID = "16d4387b-8054-4c95-9bd2-a7349d503160";
const ATTEMPT_ID = "2d73e56b-69cf-47bd-8dca-3716546f2cdf";

function makeEvent(screen: ReservationFunnelEvent["screen"]): ReservationFunnelEvent {
  return {
    sessionId: SESSION_ID,
    reservationAttemptId: ATTEMPT_ID,
    eventType: "SCREEN_VIEWED",
    screen,
  };
}

describe("reservation funnel tracking", () => {
  it("reuses a valid session id already in storage", () => {
    const storage = { getItem: vi.fn().mockReturnValue(SESSION_ID), setItem: vi.fn() };
    const createId = vi.fn();

    expect(getOrCreateReservationFunnelSessionId(storage, createId)).toBe(SESSION_ID);
    expect(storage.setItem).not.toHaveBeenCalled();
    expect(createId).not.toHaveBeenCalled();
  });

  it("creates and stores a session id when storage has no valid id", () => {
    const storage = { getItem: vi.fn().mockReturnValue("invalid"), setItem: vi.fn() };
    const createId = vi.fn().mockReturnValue(SESSION_ID);

    expect(getOrCreateReservationFunnelSessionId(storage, createId)).toBe(SESSION_ID);
    expect(storage.setItem).toHaveBeenCalledWith("wayfarer.reservation-funnel-session", SESSION_ID);
  });

  it("uses a fresh id when browser storage is unavailable", () => {
    const storage = { getItem: vi.fn(() => { throw new Error("Storage is disabled"); }), setItem: vi.fn() };

    expect(getOrCreateReservationFunnelSessionId(storage, () => SESSION_ID)).toBe(SESSION_ID);
  });

  it("serializes events and continues if the analytics adapter fails", async () => {
    const recorded: string[] = [];
    const eventPort: ReservationFunnelEventPort = {
      recordReservationFunnelEvent: vi.fn()
        .mockImplementationOnce(async () => { recorded.push("checkout"); throw new Error("Offline"); })
        .mockImplementationOnce(async () => { recorded.push("hotel"); }),
    };
    const recorder = new ReservationFunnelEventRecorder(eventPort);

    await Promise.all([recorder.record(makeEvent("checkout")), recorder.record(makeEvent("hotel"))]);

    expect(recorded).toEqual(["checkout", "hotel"]);
    expect(eventPort.recordReservationFunnelEvent).toHaveBeenCalledTimes(2);
  });
});
