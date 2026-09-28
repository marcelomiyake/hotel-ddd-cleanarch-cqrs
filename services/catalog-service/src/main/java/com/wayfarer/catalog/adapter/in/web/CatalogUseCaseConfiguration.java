package com.wayfarer.catalog.adapter.in.web;

import com.wayfarer.catalog.application.GetHotelDetailsHandler;
import com.wayfarer.catalog.application.HotelCatalog;
import com.wayfarer.catalog.application.SearchHotelsHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class CatalogUseCaseConfiguration {
    @Bean
    SearchHotelsHandler searchHotelsHandler(HotelCatalog catalog) {
        return new SearchHotelsHandler(catalog);
    }

    @Bean
    GetHotelDetailsHandler getHotelDetailsHandler(HotelCatalog catalog) {
        return new GetHotelDetailsHandler(catalog);
    }
}
