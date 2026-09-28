export type SearchCriteria = {
  city: string;
  checkIn: string;
  checkOut: string;
  guests: number;
  rooms: number;
};

export function localDateAtOffset(daysAhead: number, today = new Date()): string {
  const date = new Date(today.getFullYear(), today.getMonth(), today.getDate() + daysAhead);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function defaultSearch(today = new Date()): SearchCriteria {
  return {
    city: "Rio de Janeiro",
    checkIn: localDateAtOffset(14, today),
    checkOut: localDateAtOffset(17, today),
    guests: 2,
    rooms: 1,
  };
}

export function nightsBetween(checkIn: string, checkOut: string): number {
  const start = Date.parse(`${checkIn}T00:00:00Z`);
  const end = Date.parse(`${checkOut}T00:00:00Z`);
  if (!Number.isFinite(start) || !Number.isFinite(end) || end <= start) {
    return 0;
  }
  return (end - start) / 86_400_000;
}

export function validateSearch(criteria: SearchCriteria, today = new Date()): string | null {
  if (!criteria.city.trim()) return "Choose a destination to see available stays.";
  if (nightsBetween(criteria.checkIn, criteria.checkOut) === 0) {
    return "Check-out must be after check-in.";
  }
  const todayIso = localDateAtOffset(0, today);
  if (criteria.checkIn < todayIso) return "Check-in cannot be in the past.";
  if (nightsBetween(criteria.checkIn, criteria.checkOut) > 30) {
    return "Choose a stay of 30 nights or fewer.";
  }
  if (!Number.isInteger(criteria.guests) || criteria.guests < 1 || criteria.guests > 12) {
    return "Guest count must be between 1 and 12.";
  }
  if (!Number.isInteger(criteria.rooms) || criteria.rooms < 1 || criteria.rooms > 3) {
    return "Choose between 1 and 3 rooms.";
  }
  return null;
}

export function formatStay(checkIn: string, checkOut: string, locale = "en-BR"): string {
  const start = new Date(`${checkIn}T12:00:00`);
  const end = new Date(`${checkOut}T12:00:00`);
  const formatter = new Intl.DateTimeFormat(locale, { day: "numeric", month: "short" });
  return `${formatter.format(start)} – ${formatter.format(end)}`;
}

export function formatMoney(cents: number, currency = "BRL", locale = "pt-BR"): string {
  return new Intl.NumberFormat(locale, { style: "currency", currency, maximumFractionDigits: 0 })
    .format(cents / 100);
}
