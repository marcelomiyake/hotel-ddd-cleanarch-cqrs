package com.wayfarer.reservation.adapter.in.web;

import com.wayfarer.reservation.application.CancelReservationHandler;
import com.wayfarer.reservation.application.PlaceReservationCommand;
import com.wayfarer.reservation.application.PlaceReservationHandler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
public class ReservationCommandController {
    private final PlaceReservationHandler placeReservation;
    private final CancelReservationHandler cancelReservation;

    public ReservationCommandController(PlaceReservationHandler placeReservation,
                                        CancelReservationHandler cancelReservation) {
        this.placeReservation = placeReservation;
        this.cancelReservation = cancelReservation;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse place(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                     @Valid @RequestBody PlaceReservationRequest request) {
        return ReservationResponse.from(placeReservation.handle(new PlaceReservationCommand(
                idempotencyKey, request.hotelId(), request.roomTypeId(), request.guestName(), request.guestEmail(),
                request.checkIn(), request.checkOut(), request.guests(), request.rooms())));
    }

    @DeleteMapping("/{reservationId}")
    public ReservationResponse cancel(@PathVariable UUID reservationId, @RequestParam String email) {
        return ReservationResponse.from(cancelReservation.handle(reservationId, email));
    }
}
