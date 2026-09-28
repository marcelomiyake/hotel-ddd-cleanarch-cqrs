package com.wayfarer.reservation.adapter.in.web;

import com.wayfarer.reservation.domain.DomainRuleViolation;
import com.wayfarer.reservation.domain.IdempotencyConflictException;
import com.wayfarer.reservation.domain.ReservationAccessDeniedException;
import com.wayfarer.reservation.domain.ReservationNotFoundException;
import com.wayfarer.reservation.domain.RoomUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReservationExceptionHandler {
    @ExceptionHandler({RoomUnavailableException.class, IdempotencyConflictException.class})
    public ProblemDetail conflict(DomainRuleViolation exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(DomainRuleViolation.class)
    public ProblemDetail invalidRequest(DomainRuleViolation exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ProblemDetail notFound(ReservationNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ReservationAccessDeniedException.class)
    public ProblemDetail forbidden(ReservationAccessDeniedException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
    }
}
