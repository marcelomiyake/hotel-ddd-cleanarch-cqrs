import type { ReservationFunnelEvent } from "../domain/reservation-funnel";
import type { ReservationFunnelEventPort } from "./ports";

const SESSION_ID_KEY = "wayfarer.reservation-funnel-session";
const UUID_PATTERN = /^[\da-f]{8}(?:-[\da-f]{4}){3}-[\da-f]{12}$/i;

export interface SessionStoragePort {
  getItem(key: string): string | null;
  setItem(key: string, value: string): void;
}

export function getOrCreateReservationFunnelSessionId(storage: SessionStoragePort, createId: () => string): string {
  try {
    const stored = storage.getItem(SESSION_ID_KEY);
    if (stored && UUID_PATTERN.test(stored)) return stored;
    const created = createId();
    storage.setItem(SESSION_ID_KEY, created);
    return created;
  } catch {
    return createId();
  }
}

export class ReservationFunnelEventRecorder {
  private pending: Promise<void> = Promise.resolve();

  constructor(private readonly eventPort: ReservationFunnelEventPort) {}

  record(event: ReservationFunnelEvent): Promise<void> {
    this.pending = this.pending
      .then(() => this.eventPort.recordReservationFunnelEvent(event))
      .catch(() => undefined);
    return this.pending;
  }
}
