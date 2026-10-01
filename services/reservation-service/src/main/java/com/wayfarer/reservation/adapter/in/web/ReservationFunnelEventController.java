package com.wayfarer.reservation.adapter.in.web;

import com.wayfarer.reservation.application.RecordReservationFunnelEventCommand;
import com.wayfarer.reservation.application.RecordReservationFunnelEventHandler;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservation-funnel-events")
public class ReservationFunnelEventController {
    private final RecordReservationFunnelEventHandler recordEvent;

    public ReservationFunnelEventController(RecordReservationFunnelEventHandler recordEvent) {
        this.recordEvent = recordEvent;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordEvent(@Valid @RequestBody ReservationFunnelEventRequest request) {
        recordEvent.handle(new RecordReservationFunnelEventCommand(request.sessionId(),
                request.reservationAttemptId(), request.eventType(), request.screen(),
                request.hotelId(), request.roomTypeId()));
    }
}
