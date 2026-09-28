package com.wayfarer.reservation.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record PlaceReservationRequest(@NotBlank String hotelId, @NotBlank String roomTypeId,
        @NotBlank String guestName, @NotBlank @Email String guestEmail,
        @NotNull LocalDate checkIn, @NotNull LocalDate checkOut,
        @Min(1) @Max(12) int guests, @Min(1) @Max(3) int rooms) {
}
