package com.wayfarer.reservation.domain;

public enum ReservationFunnelScreen {
    HOME("home"),
    RESULTS("results"),
    HOTEL("hotel"),
    CHECKOUT("checkout"),
    CONFIRMATION("confirmation"),
    TRIPS("trips");

    private final String value;

    ReservationFunnelScreen(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
