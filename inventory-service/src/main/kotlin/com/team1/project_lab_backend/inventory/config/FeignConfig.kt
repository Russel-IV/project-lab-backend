package com.team1.project_lab_backend.inventory.config

import feign.Client
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import feign.okhttp.OkHttpClient as FeignOkHttpClient

@Configuration
class FeignConfig {
    @Bean
    fun feignClient(
        loadBalancerClient: LoadBalancerClient,
        loadBalancerClientFactory: LoadBalancerClientFactory,
        transformers: List<LoadBalancerFeignRequestTransformer>,
    ): Client = FeignBlockingLoadBalancerClient(
        FeignOkHttpClient(),
        loadBalancerClient,
        loadBalancerClientFactory,
        transformers
    )
}
