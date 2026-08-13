package com.project.lab.chatbotService.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.project.lab.chatbotService.model.Stay
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.math.BigDecimal

/**
 * Client responsible for sending GraphQL requests to the project-lab-backend service.
 */
@Component
class FruiBackendClient(
    @Value("\${frui.backend.graphql.url:http://localhost:8080/graphql}")
    private val graphqlUrl: String
) {
    private val logger = LoggerFactory.getLogger(FruiBackendClient::class.java)
    private val objectMapper = ObjectMapper()
    
    private val restClient: RestClient by lazy {
        RestClient.builder().baseUrl(graphqlUrl).build()
    }

    /**
     * Request payload structure for GraphQL.
     */
    data class GraphQLRequest(
        val query: String,
        val variables: Map<String, Any?> = emptyMap()
    )

    /**
     * Response payload structure for GraphQL.
     */
    data class GraphQLResponse<T>(
        val data: T? = null,
        val errors: List<Map<String, Any>>? = null
    )

    /**
     * Search stays using filters against the backend service GraphQL endpoint.
     */
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
        val query = """
            query(${'$'}filter: StayFilterInput) {
                stays(filter: ${'$'}filter, page: 0, size: 15) {
                    items {
                        id
                        name
                        about
                        propertyType
                        isRefundable
                        starRating
                        startingFromPrice
                        address {
                            streetAddress
                            city
                            countryCode
                        }
                    }
                    totalCount
                    hasNextPage
                }
            }
        """.trimIndent()

        val filter = mutableMapOf<String, Any?>()
        if (city != null) filter["city"] = city
        if (countryCode != null) filter["countryCode"] = countryCode
        if (propertyType != null) filter["propertyType"] = propertyType
        if (minPrice != null) filter["minPricePerNight"] = minPrice
        if (maxPrice != null) filter["maxPricePerNight"] = maxPrice
        if (guests != null) filter["guests"] = guests
        if (checkIn != null) filter["checkIn"] = checkIn
        if (checkOut != null) filter["checkOut"] = checkOut

        val variables = mapOf("filter" to filter)

        try {
            val responseString = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(GraphQLRequest(query, variables))
                .retrieve()
                .body(String::class.java) ?: return emptyList()

            val typeRef = object : TypeReference<GraphQLResponse<Map<String, Any>>>() {}
            val parsed = objectMapper.readValue(responseString, typeRef)
            
            if (parsed.errors != null && parsed.errors.isNotEmpty()) {
                logger.error("GraphQL Search Errors: {}", parsed.errors)
            }

            val staysConnection = parsed.data?.get("stays") as? Map<*, *>
            val itemsData = staysConnection?.get("items") ?: return emptyList()
            return objectMapper.convertValue(itemsData, object : TypeReference<List<Stay>>() {})
        } catch (e: Exception) {
            logger.error("Failed to query stays from backend", e)
            return emptyList()
        }
    }

    /**
     * Fetch stay details by ID.
     */
    fun getStayDetails(id: Int): Stay? {
        val query = """
            query(${'$'}id: Int!) {
                stay(id: ${'$'}id) {
                    id
                    name
                    about
                    propertyType
                    isRefundable
                    starRating
                    policiesText
                    importantInformation
                    address {
                        streetAddress
                        city
                        stateProvince
                        postalCode
                        countryCode
                    }
                    host {
                        name
                    }
                    amenities {
                        name
                    }
                    views {
                        name
                    }
                    startingFromPrice
                }
            }
        """.trimIndent()

        val variables = mapOf("id" to id)

        try {
            val responseString = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(GraphQLRequest(query, variables))
                .retrieve()
                .body(String::class.java) ?: return null

            val typeRef = object : TypeReference<GraphQLResponse<Map<String, Any>>>() {}
            val parsed = objectMapper.readValue(responseString, typeRef)
            
            if (parsed.errors != null && parsed.errors.isNotEmpty()) {
                logger.error("GraphQL Stay Details Errors: {}", parsed.errors)
            }

            val stayData = parsed.data?.get("stay") ?: return null
            return objectMapper.convertValue(stayData, Stay::class.java)
        } catch (e: Exception) {
            logger.error("Failed to fetch stay details for id $id", e)
            return null
        }
    }
}
