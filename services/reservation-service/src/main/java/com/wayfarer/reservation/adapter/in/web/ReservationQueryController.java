package com.wayfarer.reservation.adapter.in.web;

import com.wayfarer.reservation.application.AvailabilityQuery;
import com.wayfarer.reservation.application.FindReservationsHandler;
import com.wayfarer.reservation.application.GetAvailabilityHandler;
import com.wayfarer.reservation.application.GetReservationHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class ReservationQueryController {
    private final GetAvailabilityHandler getAvailability;
    private final GetReservationHandler getReservation;
    private final FindReservationsHandler findReservations;

    public ReservationQueryController(GetAvailabilityHandler getAvailability,
                                      GetReservationHandler getReservation,
                                      FindReservationsHandler findReservations) {
        this.getAvailability = getAvailability;
        this.getReservation = getReservation;
        this.findReservations = findReservations;
    }

    @GetMapping("/api/availability")
    public List<AvailabilityResponse> availability(@RequestParam LocalDate checkIn,
                                                   @RequestParam LocalDate checkOut,
                                                   @RequestParam int guests) {
        return getAvailability.handle(new AvailabilityQuery(checkIn, checkOut, guests)).stream()
                .map(AvailabilityResponse::from)
                .toList();
    }

    @GetMapping("/api/reservations")
    public List<ReservationResponse> history(@RequestParam String email) {
        return findReservations.handle(email).stream().map(ReservationResponse::from).toList();
    }

    @GetMapping("/api/reservations/{reservationId}")
    public ReservationResponse details(@PathVariable UUID reservationId, @RequestParam String email) {
        return ReservationResponse.from(getReservation.handle(reservationId, email));
    }
}
