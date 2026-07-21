package com.team1.project_lab_backend.config

import io.micrometer.observation.ObservationRegistry
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

/**
 * docs/adr/0025: replaces FeignConfig.kt as the gateway's outbound-call
 * mechanism. @LoadBalanced resolves a bare service-name host (e.g.
 * "http://inventory-service") against Eureka via Spring Cloud LoadBalancer's
 * reactive ExchangeFilterFunction — the WebClient equivalent of
 * FeignBlockingLoadBalancerClient. Unlike Feign's default client, WebClient
 * supports PATCH natively, so the OkHttp workaround FeignConfig needed
 * retires along with it.
 *
 * One named, already-baseUrl'd WebClient bean per target service (rather than
 * each *FeignClient class calling .baseUrl(...) on the builder itself) so
 * contract tests (e.g. BookingFeignClientTest) can construct a client against
 * a WireMock server directly, by passing a differently-configured WebClient —
 * that's not possible if the base URL is hardcoded inside the client's own
 * constructor.
 */
@Configuration
class WebClientConfig {
    /**
     * Two separate hooks, both required, confirmed the hard way via live Zipkin
     * checks after fixing only one at a time:
     * - `.clientConnector(connector)`: the injected ClientHttpConnector (from
     *   spring-boot-http-client's ReactiveHttpClientAutoConfiguration, not
     *   transitively pulled in by spring-boot-starter-webflux alone — added
     *   explicitly, see pom.xml).
     * - `.observationRegistry(registry)`: without this, WebClient never creates a
     *   CLIENT-kind span per outbound call at all, so there's nothing to carry
     *   trace-context headers into the downstream request regardless of which
     *   connector is underneath it — the two are independent WebClient.Builder
     *   settings, not one production of the other.
     * A bare `WebClient.builder()` with neither set uses Reactor Netty's raw
     * defaults and silently propagates no trace context downstream — confirmed
     * live via Zipkin before this fix (gateway's own spans reached Zipkin, but no
     * span from any call ever continued into inventory-service/media-service/etc).
     */
    @Bean
    @LoadBalanced
    fun loadBalancedWebClientBuilder(
        connector: ClientHttpConnector,
        observationRegistry: ObservationRegistry,
    ): WebClient.Builder =
        WebClient.builder()
            .clientConnector(connector)
            .observationRegistry(observationRegistry)
    @Bean
    @LoadBalanced
    fun loadBalancedWebClientBuilder(): WebClient.Builder = WebClient.builder()

    @Bean("inventoryServiceWebClient")
    fun inventoryServiceWebClient(builder: WebClient.Builder): WebClient = builder.baseUrl("http://inventory-service").build()

    @Bean("identityServiceWebClient")
    fun identityServiceWebClient(builder: WebClient.Builder): WebClient = builder.baseUrl("http://identity-service").build()

    @Bean("bookingServiceWebClient")
    fun bookingServiceWebClient(builder: WebClient.Builder): WebClient = builder.baseUrl("http://booking-service").build()

    @Bean("reviewServiceWebClient")
    fun reviewServiceWebClient(builder: WebClient.Builder): WebClient = builder.baseUrl("http://review-service").build()

    @Bean("mediaServiceWebClient")
    fun mediaServiceWebClient(builder: WebClient.Builder): WebClient = builder.baseUrl("http://media-service").build()

    @Bean("chatbotServiceWebClient")
    fun chatbotServiceWebClient(builder: WebClient.Builder): WebClient = builder.baseUrl("http://chatbot-service").build()
}
