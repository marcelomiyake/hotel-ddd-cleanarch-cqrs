import type { HotelReadPort, ReservationReadPort } from "./ports";
import type { HotelCard, HotelDetails, RoomAvailability } from "../domain/hotel";
import type { SearchCriteria } from "../domain/stay";
import { validateSearch } from "../domain/stay";

export class SearchHotelsQuery {
  constructor(private readonly hotels: HotelReadPort) {}

  execute(city: string): Promise<HotelCard[]> {
    if (!city.trim()) return Promise.reject(new Error("Choose a destination to search."));
    return this.hotels.searchHotels(city.trim());
  }
}

export class GetHotelDetailsQuery {
  constructor(private readonly hotels: HotelReadPort) {}

  execute(hotelId: string): Promise<HotelDetails> {
    if (!hotelId.trim()) return Promise.reject(new Error("A hotel id is required."));
    return this.hotels.getHotel(hotelId.trim());
  }
}

export class FindAvailableRoomsQuery {
  constructor(private readonly reservations: ReservationReadPort) {}

  execute(criteria: SearchCriteria): Promise<RoomAvailability[]> {
    const error = validateSearch(criteria);
    if (error) return Promise.reject(new Error(error));
    return this.reservations.getAvailability(criteria);
  }
}
