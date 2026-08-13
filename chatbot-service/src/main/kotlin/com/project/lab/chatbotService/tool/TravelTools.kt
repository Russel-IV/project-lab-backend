package com.project.lab.chatbotService.tool

import com.project.lab.chatbotService.client.FruiBackendClient
import com.project.lab.chatbotService.context.ChatContext
import com.project.lab.chatbotService.controller.ChatController.StaySummary
import com.project.lab.chatbotService.model.Stay
import org.springframework.ai.tool.annotation.Tool
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * Declarative Spring AI tool component exposing functions to the LLM.
 */
@Component
class TravelTools(
    private val fruiBackendClient: FruiBackendClient,
    private val chatContext: ChatContext
) {

    /**
     * Search stays by location, dates, price, and capacity matching searchForm criteria.
     */
    @Tool(description = "Search stays and accommodations matching searchForm criteria. Pass ONLY the clean city name for 'city' (e.g. 'Valparaíso', 'Miami', 'Barcelona') without country/state suffixes (do NOT pass 'Valparaíso, Chile'). Accents and spelling diacritics are handled automatically.")
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
        val stays = fruiBackendClient.searchStays(
            city = city,
            countryCode = countryCode,
            propertyType = propertyType,
            minPrice = minPrice,
            maxPrice = maxPrice,
            guests = guests,
            checkIn = checkIn,
            checkOut = checkOut
        )

        val summaries = stays.mapNotNull { stay ->
            val pubId = stay.publicId ?: return@mapNotNull null
            val primaryPicture = stay.pictures?.find { it.isPrimary == true } ?: stay.pictures?.firstOrNull()
            val imgUrl = primaryPicture?.thumbnailUrl ?: primaryPicture?.url512 ?: primaryPicture?.url
            StaySummary(
                id = stay.id ?: 0,
                publicId = pubId,
                name = stay.name ?: "Property Listing",
                propertyType = stay.propertyType?.name ?: "HOTEL",
                starRating = stay.starRating,
                startingFromPrice = stay.startingFromPrice,
                city = stay.address?.city,
                countryCode = stay.address?.countryCode,
                imageUrl = imgUrl
            )
        }

        chatContext.setStays(summaries)
        return stays
    }

    /**
     * Fetch stay details by ID.
     */
    @Tool(description = "Retrieve detailed information about a specific stay by its unique integer ID, including host, full address, rules, amenities, views, and policies.")
    fun getStayDetails(id: Int): Stay? {
        return fruiBackendClient.getStayDetails(id)
    }
}
