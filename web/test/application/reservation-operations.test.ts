import { describe, expect, it, vi } from "vitest";
import type { PlaceReservationInput, Reservation } from "../../src/domain/reservation";
import type { ReservationCommandPort, ReservationReadPort } from "../../src/application/ports";
import { CancelReservationCommand, FindReservationsQuery, PlaceReservationCommand } from "../../src/application/reservation-operations";

const reservation: Reservation = {
  id: "reservation-1", hotelId: "hotel-1", roomTypeId: "room-1", roomName: "Terrace King",
  guestName: "Guest One", guestEmail: "guest@example.com", checkIn: "2026-10-12", checkOut: "2026-10-15",
  nights: 3, guests: 2, rooms: 1, status: "CONFIRMED", pricePerNightCents: 10000,
  totalPriceCents: 30000, currency: "BRL", createdAt: "2026-09-28T12:00:00Z",
};

function makePorts() {
  const commandPort: ReservationCommandPort = {
    placeReservation: vi.fn().mockResolvedValue(reservation),
    cancelReservation: vi.fn().mockResolvedValue({ ...reservation, status: "CANCELLED" }),
  };
  const readPort: ReservationReadPort = {
    getAvailability: vi.fn().mockResolvedValue([]),
    findReservations: vi.fn().mockResolvedValue([reservation]),
  };
  return { commandPort, readPort };
}

const input: PlaceReservationInput = {
  city: "Rio", checkIn: "2026-10-12", checkOut: "2026-10-15", guests: 2, rooms: 1,
  hotelId: "hotel-1", roomTypeId: "room-1", guestName: " Guest One ", guestEmail: " GUEST@example.com ",
};

describe("reservation use cases", () => {
  it("normalizes guest details before placing a reservation", async () => {
    const { commandPort } = makePorts();
    await expect(new PlaceReservationCommand(commandPort).execute(input, " key-1 ")).resolves.toEqual(reservation);
    expect(commandPort.placeReservation).toHaveBeenCalledWith({ ...input, guestName: "Guest One", guestEmail: "guest@example.com" }, "key-1");
  });

  it("rejects invalid guest and incomplete reservation input", async () => {
    const { commandPort } = makePorts();
    const command = new PlaceReservationCommand(commandPort);
    await expect(command.execute({ ...input, guestEmail: "bad" }, "key-1")).rejects.toThrow(/valid email/);
    await expect(command.execute({ ...input, roomTypeId: "" }, "key-1")).rejects.toThrow(/Choose a room/);
    await expect(command.execute(input, " ")).rejects.toThrow(/Choose a room/);
    expect(commandPort.placeReservation).not.toHaveBeenCalled();
  });

  it("looks up reservations using a normalized email", async () => {
    const { readPort } = makePorts();
    await expect(new FindReservationsQuery(readPort).execute(" GUEST@example.com ")).resolves.toEqual([reservation]);
    expect(readPort.findReservations).toHaveBeenCalledWith("guest@example.com");
  });

  it("validates the lookup email", async () => {
    const { readPort } = makePorts();
    await expect(new FindReservationsQuery(readPort).execute("not-email")).rejects.toThrow(/email used/);
    expect(readPort.findReservations).not.toHaveBeenCalled();
  });

  it("cancels with the guest email and normalizes it", async () => {
    const { commandPort } = makePorts();
    await expect(new CancelReservationCommand(commandPort).execute(" reservation-1 ", " GUEST@example.com "))
      .resolves.toMatchObject({ status: "CANCELLED" });
    expect(commandPort.cancelReservation).toHaveBeenCalledWith("reservation-1", "guest@example.com");
  });

  it("rejects a missing reservation id or invalid cancel email", async () => {
    const { commandPort } = makePorts();
    const command = new CancelReservationCommand(commandPort);
    await expect(command.execute(" ", "guest@example.com")).rejects.toThrow(/Check the reservation/);
    await expect(command.execute("reservation-1", "bad")).rejects.toThrow(/Check the reservation/);
    expect(commandPort.cancelReservation).not.toHaveBeenCalled();
  });
});
