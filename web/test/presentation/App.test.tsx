import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import type { HotelCard, HotelDetails, RoomAvailability } from "../../src/domain/hotel";
import type { Reservation } from "../../src/domain/reservation";
import type { HotelReadPort, ReservationCommandPort, ReservationFunnelEventPort, ReservationReadPort } from "../../src/application/ports";
import { App } from "../../src/presentation/App";

const hotel: HotelCard = {
  id: "rio-casa-do-mar", name: "Casa do Mar", city: "Rio de Janeiro", country: "Brazil",
  address: "Av. Vieira Souto, 170", starRating: 5, guestRating: 9.6, reviewCount: 842,
  priceFromCents: 148000, currency: "BRL", tagline: "A quiet corner of Ipanema.",
  imageUrl: "/hotels/casa-mar.webp", highlights: ["Ocean view", "Breakfast included"], featured: true,
};
const details: HotelDetails = {
  hotel,
  description: "A bright place beside the Atlantic.",
  locationSummary: "Ipanema · 2 min walk to the beach",
  gallery: ["/hotels/casa-mar.webp", "/hotels/casa-mar-room.webp", "/hotels/casa-mar-pool.webp"],
  rooms: [{ id: "room-casa-mar-terrace", name: "Terrace King", description: "A light room with a private terrace.",
    bedSummary: "1 king bed", maxGuests: 2, pricePerNightCents: 148000, currency: "BRL", amenities: ["Ocean glimpse", "Breakfast"] }],
};
const offer: RoomAvailability = {
  hotelId: hotel.id, roomTypeId: "room-casa-mar-terrace", roomName: "Terrace King", availableRooms: 4,
  maxGuestsPerRoom: 2, pricePerNightCents: 148000, currency: "BRL",
};
const booking: Reservation = {
  id: "f13db4ac-1bda-45e2-ae02-7bd0018ae14a", hotelId: hotel.id,
  roomTypeId: offer.roomTypeId, roomName: offer.roomName,
  guestName: "Alex Traveler", guestEmail: "alex@example.com",
  checkIn: "2026-10-12", checkOut: "2026-10-15", nights: 3, guests: 2, rooms: 1,
  status: "CONFIRMED", pricePerNightCents: 148000, totalPriceCents: 444000,
  currency: "BRL", createdAt: "2026-09-28T12:00:00Z",
};

function setupApp() {
  const hotelGateway: HotelReadPort = {
    searchHotels: vi.fn().mockResolvedValue([hotel]),
    getHotel: vi.fn().mockResolvedValue(details),
  };
  const reservationGateway: ReservationReadPort & ReservationCommandPort = {
    getAvailability: vi.fn().mockResolvedValue([offer]),
    findReservations: vi.fn().mockResolvedValue([booking]),
    placeReservation: vi.fn().mockResolvedValue(booking),
    cancelReservation: vi.fn().mockResolvedValue({ ...booking, status: "CANCELLED" }),
  };
  const reservationFunnelGateway: ReservationFunnelEventPort = {
    recordReservationFunnelEvent: vi.fn().mockResolvedValue(undefined),
  };
  return { hotelGateway, reservationGateway, reservationFunnelGateway };
}

describe("hotel reservation journeys", () => {
  it("searches, chooses a room, confirms a reservation, and cancels it from My trips", async () => {
    const user = userEvent.setup();
    const { hotelGateway, reservationGateway, reservationFunnelGateway } = setupApp();
    render(<App hotelGateway={hotelGateway} reservationGateway={reservationGateway} reservationFunnelGateway={reservationFunnelGateway} />);

    expect(await screen.findByRole("heading", { name: /Places with/i })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Search stays" }));
    expect(await screen.findByRole("heading", { name: "Stays in Rio de Janeiro" })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: /View Casa do Mar/ }));
    expect(await screen.findByRole("heading", { name: "Casa do Mar" })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Choose room" }));

    await user.type(await screen.findByLabelText("Full name"), "Alex Traveler");
    await user.type(await screen.findByLabelText("Email address"), "Alex@Example.com");
    await user.click(screen.getByRole("button", { name: /Confirm reservation/ }));
    expect(await screen.findByRole("heading", { name: /Your time away/i })).toBeInTheDocument();
    expect(reservationGateway.placeReservation).toHaveBeenCalledWith(
      expect.objectContaining({ guestName: "Alex Traveler", guestEmail: "alex@example.com", roomTypeId: offer.roomTypeId }),
      expect.any(String),
    );
    await waitFor(() => expect(reservationFunnelGateway.recordReservationFunnelEvent).toHaveBeenCalledTimes(2));
    expect(reservationFunnelGateway.recordReservationFunnelEvent).toHaveBeenNthCalledWith(1,
      expect.objectContaining({ eventType: "RESERVATION_STARTED", screen: "checkout", hotelId: hotel.id, roomTypeId: offer.roomTypeId }));
    expect(reservationFunnelGateway.recordReservationFunnelEvent).toHaveBeenNthCalledWith(2,
      expect.objectContaining({ eventType: "RESERVATION_CONFIRMED", screen: "confirmation", hotelId: hotel.id, roomTypeId: offer.roomTypeId }));

    await user.click(screen.getByRole("button", { name: "My trips" }));
    expect(await screen.findByLabelText("Email used for your reservation")).toHaveValue("alex@example.com");
    await user.click(screen.getByRole("button", { name: "Find my trips" }));
    expect(await screen.findByRole("heading", { name: "Casa do Mar" })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Cancel reservation" }));
    expect(await screen.findByText("Cancelled")).toBeInTheDocument();
    expect(reservationGateway.cancelReservation).toHaveBeenCalledWith(booking.id, booking.guestEmail);
  });

  it("shows empty collection and API errors without blocking the home screen", async () => {
    const hotelGateway: HotelReadPort = {
      searchHotels: vi.fn().mockRejectedValueOnce(new Error("Catalog is warming up.")).mockResolvedValue([hotel]),
      getHotel: vi.fn().mockRejectedValue(new Error("Hotel is unavailable.")),
    };
    const reservationGateway: ReservationReadPort & ReservationCommandPort = {
      getAvailability: vi.fn().mockRejectedValue(new Error("Inventory is unavailable.")),
      findReservations: vi.fn().mockResolvedValue([]),
      placeReservation: vi.fn().mockRejectedValue(new Error("Reservation is unavailable.")),
      cancelReservation: vi.fn().mockRejectedValue(new Error("Cancellation is unavailable.")),
    };
    const reservationFunnelGateway: ReservationFunnelEventPort = { recordReservationFunnelEvent: vi.fn().mockResolvedValue(undefined) };
    const user = userEvent.setup();
    render(<App hotelGateway={hotelGateway} reservationGateway={reservationGateway} reservationFunnelGateway={reservationFunnelGateway} />);
    expect(await screen.findByText("Good stays are on their way.")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Search stays" }));
    expect(await screen.findByText("Inventory is unavailable.")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: /Places with/i })).toBeInTheDocument();
  });

  it("keeps empty search results and an empty trips lookup actionable", async () => {
    const user = userEvent.setup();
    const { hotelGateway, reservationGateway, reservationFunnelGateway } = setupApp();
    vi.mocked(hotelGateway.searchHotels).mockResolvedValueOnce([hotel]).mockResolvedValueOnce([]);
    vi.mocked(reservationGateway.getAvailability).mockResolvedValueOnce([offer]).mockResolvedValueOnce([]);
    vi.mocked(reservationGateway.findReservations).mockResolvedValueOnce([]);
    render(<App hotelGateway={hotelGateway} reservationGateway={reservationGateway} reservationFunnelGateway={reservationFunnelGateway} />);
    await screen.findByRole("heading", { name: /Places with/i });
    fireEvent.change(screen.getByLabelText("Where to?"), { target: { value: "São Paulo" } });
    await user.click(screen.getByRole("button", { name: "Search stays" }));
    expect(await screen.findByRole("heading", { name: "No stays found for those dates." })).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "My trips" }));
    await user.type(await screen.findByLabelText("Email used for your reservation"), "traveler@example.com");
    await user.click(screen.getByRole("button", { name: "Find my trips" }));
    expect(await screen.findByText("Your next stay starts here.")).toBeInTheDocument();
  });

  it("tracks the last reservation screen when a guest returns to hotel details", async () => {
    const user = userEvent.setup();
    const { hotelGateway, reservationGateway, reservationFunnelGateway } = setupApp();
    render(<App hotelGateway={hotelGateway} reservationGateway={reservationGateway} reservationFunnelGateway={reservationFunnelGateway} />);

    await screen.findByRole("heading", { name: /Places with/i });
    await user.click(screen.getByRole("button", { name: "Search stays" }));
    await user.click(await screen.findByRole("button", { name: /View Casa do Mar/ }));
    await user.click(await screen.findByRole("button", { name: "Choose room" }));
    await user.click(await screen.findByRole("button", { name: "Change room" }));

    await waitFor(() => expect(reservationFunnelGateway.recordReservationFunnelEvent).toHaveBeenCalledTimes(2));
    expect(reservationFunnelGateway.recordReservationFunnelEvent).toHaveBeenNthCalledWith(2,
      expect.objectContaining({ eventType: "SCREEN_VIEWED", screen: "hotel" }));
  });
});
