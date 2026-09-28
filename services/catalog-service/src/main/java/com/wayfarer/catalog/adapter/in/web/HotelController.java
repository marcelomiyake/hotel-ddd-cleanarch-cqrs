package com.wayfarer.catalog.adapter.in.web;

import com.wayfarer.catalog.application.GetHotelDetailsHandler;
import com.wayfarer.catalog.application.SearchHotelsHandler;
import com.wayfarer.catalog.application.SearchHotelsQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {
    private final SearchHotelsHandler searchHotels;
    private final GetHotelDetailsHandler getHotelDetails;

    public HotelController(SearchHotelsHandler searchHotels, GetHotelDetailsHandler getHotelDetails) {
        this.searchHotels = searchHotels;
        this.getHotelDetails = getHotelDetails;
    }

    @GetMapping
    public List<HotelCardResponse> search(@RequestParam(required = false) String city) {
        return searchHotels.handle(new SearchHotelsQuery(city)).stream()
                .map(HotelCardResponse::from)
                .toList();
    }

    @GetMapping("/{hotelId}")
    public HotelDetailsResponse details(@PathVariable String hotelId) {
        return HotelDetailsResponse.from(getHotelDetails.handle(hotelId));
    }
}
