package com.team1.project_lab_backend.config

import io.micrometer.observation.ObservationRegistry
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfig {
    @Bean
    @LoadBalanced
    fun loadBalancedWebClientBuilder(
        connector: ClientHttpConnector,
        observationRegistry: ObservationRegistry,
    ): WebClient.Builder =
        WebClient.builder()
            .clientConnector(connector)
            .observationRegistry(observationRegistry)

    @Bean("inventoryServiceWebClient")
    fun inventoryServiceWebClient(builder: WebClient.Builder): WebClient =
        builder.baseUrl("http://inventory-service").build()

    @Bean("identityServiceWebClient")
    fun identityServiceWebClient(builder: WebClient.Builder): WebClient =
        builder.baseUrl("http://identity-service").build()

    @Bean("bookingServiceWebClient")
    fun bookingServiceWebClient(builder: WebClient.Builder): WebClient =
        builder.baseUrl("http://booking-service").build()

    @Bean("reviewServiceWebClient")
    fun reviewServiceWebClient(builder: WebClient.Builder): WebClient = builder.baseUrl("http://review-service").build()

    @Bean("mediaServiceWebClient")
    fun mediaServiceWebClient(builder: WebClient.Builder): WebClient = builder.baseUrl("http://media-service").build()

    @Bean("chatbotServiceWebClient")
    fun chatbotServiceWebClient(builder: WebClient.Builder): WebClient =
        builder.baseUrl("http://chatbot-service").build()
}
