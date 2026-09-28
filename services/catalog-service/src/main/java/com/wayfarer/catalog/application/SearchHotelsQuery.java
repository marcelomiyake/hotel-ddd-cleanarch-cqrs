package com.wayfarer.catalog.application;

public record SearchHotelsQuery(String city) {
    public SearchHotelsQuery {
        city = city == null || city.isBlank() ? null : city.trim();
    }
}
