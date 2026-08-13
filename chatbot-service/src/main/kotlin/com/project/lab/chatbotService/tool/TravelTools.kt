package com.project.lab.chatbotService.tool

import com.project.lab.chatbotService.client.FruiBackendClient
import com.project.lab.chatbotService.model.Stay
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * Declarative Spring AI tool component exposing functions to the LLM.
 */
@Component
class TravelTools(private val fruiBackendClient: FruiBackendClient) {

    /**
     * Search stays by location, dates, price, and capacity matching searchForm criteria.
     */
    @Tool(description = "Search stays and accommodations matching searchForm criteria such as city, checkIn date (YYYY-MM-DD), checkOut date (YYYY-MM-DD), guests capacity, country code, property type (HOTEL or HOME), and min/max price per night.")
    fun searchStays(
        city: String? = null,
        countryCode: String? = null,
        propertyType: String? = null,
        minPrice: BigDecimal? = null,
        maxPrice: BigDecimal? = null,
        guests: Int? = null,
        checkIn: String? = null,
        checkOut: String? = null
    ): List<Stay> {
        return fruiBackendClient.searchStays(
            city = city,
            countryCode = countryCode,
            propertyType = propertyType,
            minPrice = minPrice,
            maxPrice = maxPrice,
            guests = guests,
            checkIn = checkIn,
            checkOut = checkOut
        )
    }

    /**
     * Fetch stay details by ID.
     */
    @Tool(description = "Retrieve detailed information about a specific stay by its unique integer ID, including host, full address, rules, amenities, views, and policies.")
    fun getStayDetails(id: Int): Stay? {
        return fruiBackendClient.getStayDetails(id)
    }
}
