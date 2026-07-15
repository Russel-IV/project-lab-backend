package com.team1.project_lab_backend.inventory.config

import feign.Client
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import feign.okhttp.OkHttpClient as FeignOkHttpClient

/**
 * Same OkHttp/LoadBalancer wiring gap as gateway/config/FeignConfig.kt (Phase 2's
 * finding) — this fictional spring-cloud-openfeign 5.0.2 has no OkHttp
 * auto-configuration, and DefaultFeignLoadBalancerConfiguration hardcodes
 * Client.Default as the delegate it wraps, backing off entirely once any custom
 * Client bean is present. This bean builds the same FeignBlockingLoadBalancerClient
 * wrapper the auto-config would have, with OkHttp (PATCH-capable) as its delegate.
 */
@Configuration
class FeignConfig {
    @Bean
    fun feignClient(
        loadBalancerClient: LoadBalancerClient,
        loadBalancerClientFactory: LoadBalancerClientFactory,
        transformers: List<LoadBalancerFeignRequestTransformer>,
    ): Client =
        FeignBlockingLoadBalancerClient(FeignOkHttpClient(), loadBalancerClient, loadBalancerClientFactory, transformers)
}
