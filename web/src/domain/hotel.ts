export type HotelCard = {
  id: string;
  name: string;
  city: string;
  country: string;
  address: string;
  starRating: number;
  guestRating: number;
  reviewCount: number;
  priceFromCents: number;
  currency: string;
  tagline: string;
  imageUrl: string;
  highlights: string[];
  featured: boolean;
};

export type RoomType = {
  id: string;
  name: string;
  description: string;
  bedSummary: string;
  maxGuests: number;
  pricePerNightCents: number;
  currency: string;
  amenities: string[];
};

export type HotelDetails = {
  hotel: HotelCard;
  description: string;
  locationSummary: string;
  gallery: string[];
  rooms: RoomType[];
};

export type RoomAvailability = {
  hotelId: string;
  roomTypeId: string;
  roomName: string;
  availableRooms: number;
  maxGuestsPerRoom: number;
  pricePerNightCents: number;
  currency: string;
};
