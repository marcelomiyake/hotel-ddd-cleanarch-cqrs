package com.wayfarer.catalog.domain;

public final class HotelNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public HotelNotFoundException(String hotelId) {
        super("Hotel '" + hotelId + "' was not found.");
    }
}
