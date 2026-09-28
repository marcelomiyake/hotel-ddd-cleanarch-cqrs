import { describe, expect, it } from "vitest";
import { normalizeEmail, validGuest } from "../../src/domain/reservation";

describe("guest details", () => {
  it("normalizes email addresses consistently", () => {
    expect(normalizeEmail("  Guest@Example.COM ")).toBe("guest@example.com");
  });

  it("accepts a name and a well-formed email", () => {
    expect(validGuest("Guest@example.com", "A. Guest")).toBe(true);
  });

  it("rejects missing names and malformed email addresses", () => {
    expect(validGuest("guest@example.com", "  ")).toBe(false);
    expect(validGuest("not-an-email", "Guest")).toBe(false);
  });
});
