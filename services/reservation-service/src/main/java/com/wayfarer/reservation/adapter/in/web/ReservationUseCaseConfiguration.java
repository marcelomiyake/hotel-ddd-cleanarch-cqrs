package com.wayfarer.reservation.adapter.in.web;

import com.wayfarer.reservation.adapter.out.postgres.SpringTransactionBoundary;
import com.wayfarer.reservation.application.CancelReservationHandler;
import com.wayfarer.reservation.application.FindReservationsHandler;
import com.wayfarer.reservation.application.GetAvailabilityHandler;
import com.wayfarer.reservation.application.GetReservationHandler;
import com.wayfarer.reservation.application.PlaceReservationHandler;
import com.wayfarer.reservation.application.RecordReservationFunnelEventHandler;
import com.wayfarer.reservation.application.ReservationIdGenerator;
import com.wayfarer.reservation.application.ReservationFunnelEventStore;
import com.wayfarer.reservation.application.ReservationQueries;
import com.wayfarer.reservation.application.ReservationStore;
import com.wayfarer.reservation.application.RoomInventory;
import com.wayfarer.reservation.application.TransactionBoundary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.util.UUID;

@Configuration
class ReservationUseCaseConfiguration {
    @Bean
    Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    ReservationIdGenerator reservationIdGenerator() {
        return UUID::randomUUID;
    }

    @Bean
    TransactionBoundary transactionBoundary(PlatformTransactionManager manager) {
        return new SpringTransactionBoundary(new TransactionTemplate(manager));
    }

    @Bean
    PlaceReservationHandler placeReservationHandler(ReservationStore store, RoomInventory inventory,
                                                   TransactionBoundary transactions, ReservationIdGenerator ids,
                                                   Clock clock) {
        return new PlaceReservationHandler(store, inventory, transactions, ids, clock);
    }

    @Bean
    RecordReservationFunnelEventHandler recordReservationFunnelEventHandler(ReservationFunnelEventStore eventStore,
                                                                            Clock clock) {
        return new RecordReservationFunnelEventHandler(eventStore, clock);
    }

    @Bean
    CancelReservationHandler cancelReservationHandler(ReservationQueries queries, ReservationStore store,
                                                      RoomInventory inventory, TransactionBoundary transactions,
                                                      Clock clock) {
        return new CancelReservationHandler(queries, store, inventory, transactions, clock);
    }

    @Bean
    GetReservationHandler getReservationHandler(ReservationQueries queries) {
        return new GetReservationHandler(queries);
    }

    @Bean
    FindReservationsHandler findReservationsHandler(ReservationQueries queries) {
        return new FindReservationsHandler(queries);
    }

    @Bean
    GetAvailabilityHandler getAvailabilityHandler(RoomInventory inventory, Clock clock) {
        return new GetAvailabilityHandler(inventory, clock);
    }
}
