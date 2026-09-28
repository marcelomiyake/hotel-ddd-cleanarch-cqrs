import { describe, expect, it, vi } from "vitest";
import type { HotelCard, HotelDetails, RoomAvailability } from "../../src/domain/hotel";
import type { HotelReadPort, ReservationReadPort } from "../../src/application/ports";
import { FindAvailableRoomsQuery, GetHotelDetailsQuery, SearchHotelsQuery } from "../../src/application/hotel-queries";

const hotel: HotelCard = {
  id: "rio-casa-do-mar", name: "Casa do Mar", city: "Rio de Janeiro", country: "Brazil",
  address: "Ipanema", starRating: 5, guestRating: 9.6, reviewCount: 842,
  priceFromCents: 148000, currency: "BRL", tagline: "A quiet corner of Ipanema.",
  imageUrl: "/hotels/casa-mar.webp", highlights: ["Ocean view"], featured: true,
};
const details: HotelDetails = {
  hotel, description: "A small hotel by the sea.", locationSummary: "Near Ipanema beach",
  gallery: [hotel.imageUrl], rooms: [],
};
const availability: RoomAvailability[] = [{
  hotelId: hotel.id, roomTypeId: "room-1", roomName: "Terrace King", availableRooms: 4,
  maxGuestsPerRoom: 2, pricePerNightCents: 148000, currency: "BRL",
}];

function makePorts() {
  const hotelPort: HotelReadPort = {
    searchHotels: vi.fn().mockResolvedValue([hotel]),
    getHotel: vi.fn().mockResolvedValue(details),
  };
  const reservationPort: ReservationReadPort = {
    getAvailability: vi.fn().mockResolvedValue(availability),
    findReservations: vi.fn().mockResolvedValue([]),
  };
  return { hotelPort, reservationPort };
}

describe("hotel read use cases", () => {
  it("searches after trimming the destination", async () => {
    const { hotelPort } = makePorts();
    await expect(new SearchHotelsQuery(hotelPort).execute(" Rio de Janeiro ")).resolves.toEqual([hotel]);
    expect(hotelPort.searchHotels).toHaveBeenCalledWith("Rio de Janeiro");
  });

  it("rejects a blank destination", async () => {
    await expect(new SearchHotelsQuery(makePorts().hotelPort).execute(" ")).rejects.toThrow(/destination/);
  });

  it("gets a hotel by its trimmed id", async () => {
    const { hotelPort } = makePorts();
    await expect(new GetHotelDetailsQuery(hotelPort).execute(" rio-casa-do-mar ")).resolves.toEqual(details);
    expect(hotelPort.getHotel).toHaveBeenCalledWith("rio-casa-do-mar");
  });

  it("requires a hotel id", async () => {
    await expect(new GetHotelDetailsQuery(makePorts().hotelPort).execute(" ")).rejects.toThrow(/hotel id/);
  });

  it("searches availability for valid criteria", async () => {
    const { reservationPort } = makePorts();
    const query = new FindAvailableRoomsQuery(reservationPort);
    const criteria = { city: "Rio", checkIn: "2026-10-12", checkOut: "2026-10-15", guests: 2, rooms: 1 };
    await expect(query.execute(criteria)).resolves.toEqual(availability);
    expect(reservationPort.getAvailability).toHaveBeenCalledWith(criteria);
  });

  it("does not issue availability requests for invalid criteria", async () => {
    const { reservationPort } = makePorts();
    await expect(new FindAvailableRoomsQuery(reservationPort).execute({
      city: "", checkIn: "2026-10-12", checkOut: "2026-10-15", guests: 2, rooms: 1,
    })).rejects.toThrow(/destination/);
    expect(reservationPort.getAvailability).not.toHaveBeenCalled();
  });
});
