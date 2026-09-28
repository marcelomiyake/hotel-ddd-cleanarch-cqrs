import { describe, expect, it } from "vitest";
import { defaultSearch, formatMoney, formatStay, localDateAtOffset, nightsBetween, validateSearch } from "../../src/domain/stay";

const today = new Date(2026, 8, 28);
const validSearch = {
  city: "Rio de Janeiro",
  checkIn: "2026-10-12",
  checkOut: "2026-10-15",
  guests: 2,
  rooms: 1,
};

describe("stay value helpers", () => {
  it("builds a local date without shifting around UTC midnight", () => {
    expect(localDateAtOffset(0, today)).toBe("2026-09-28");
    expect(localDateAtOffset(3, today)).toBe("2026-10-01");
  });

  it("creates a useful default search window", () => {
    expect(defaultSearch(today)).toEqual({
      city: "Rio de Janeiro",
      checkIn: "2026-10-12",
      checkOut: "2026-10-15",
      guests: 2,
      rooms: 1,
    });
  });

  it("counts valid nights and rejects invalid date ranges", () => {
    expect(nightsBetween("2026-10-12", "2026-10-15")).toBe(3);
    expect(nightsBetween("2026-10-15", "2026-10-15")).toBe(0);
    expect(nightsBetween("not-a-date", "2026-10-15")).toBe(0);
  });

  it("accepts a future stay and formats its dates and price", () => {
    expect(validateSearch(validSearch, today)).toBeNull();
    expect(formatStay("2026-10-12", "2026-10-15", "en-US")).toBe("Oct 12 – Oct 15");
    expect(formatMoney(148000)).toContain("1.480");
  });

  it("requires a destination", () => {
    expect(validateSearch({ ...validSearch, city: "  " }, today)).toMatch(/destination/);
  });

  it("requires check-out to follow check-in", () => {
    expect(validateSearch({ ...validSearch, checkOut: "2026-10-12" }, today)).toMatch(/after check-in/);
  });

  it("rejects past arrivals and stays beyond thirty nights", () => {
    expect(validateSearch({ ...validSearch, checkIn: "2026-09-20", checkOut: "2026-09-23" }, today)).toMatch(/past/);
    expect(validateSearch({ ...validSearch, checkOut: "2026-11-15" }, today)).toMatch(/30 nights/);
  });

  it("limits guest and room counts to supported values", () => {
    expect(validateSearch({ ...validSearch, guests: 0 }, today)).toMatch(/Guest count/);
    expect(validateSearch({ ...validSearch, rooms: 4 }, today)).toMatch(/rooms/);
  });
});
