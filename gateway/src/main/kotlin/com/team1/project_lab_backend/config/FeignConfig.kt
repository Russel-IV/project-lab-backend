package com.team1.project_lab_backend.config

import feign.Client
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import feign.okhttp.OkHttpClient as FeignOkHttpClient

/**
 * This version of spring-cloud-openfeign (5.0.2) dropped its built-in OkHttp
 * auto-configuration (no `OkHttpFeignConfiguration`, no
 * `spring.cloud.openfeign.okhttp.enabled` property — confirmed by inspecting
 * FeignAutoConfiguration's sources; only Apache HttpClient5 and the JDK's
 * java.net.http.HttpClient are wired automatically, and neither is on this
 * module's classpath). Without a Client bean here, Feign falls back to
 * feign.Client$Default (java.net.HttpURLConnection), which cannot send PATCH.
 *
 * A bare `Client` bean is not enough: `DefaultFeignLoadBalancerConfiguration`
 * (spring-cloud-openfeign-loadbalancer) hardcodes `new Client.Default(null,
 * null)` as the delegate it wraps in `FeignBlockingLoadBalancerClient` and only
 * backs off creating that wrapper via @ConditionalOnMissingBean(Client.class)
 * — it never delegates to a custom Client bean. So a bare OkHttp Client bean
 * here disables Eureka/LoadBalancer resolution entirely: Feign then calls
 * "http://review-service/..." directly via Docker DNS with no port, which
 * defaults to :80 and gets connection-refused. This bean instead builds the
 * same FeignBlockingLoadBalancerClient wrapper the auto-config would have,
 * just with OkHttp as its delegate instead of Client.Default.
 */
@Configuration
class FeignConfig {
    @Bean
    fun feignClient(
        loadBalancerClient: LoadBalancerClient,
        loadBalancerClientFactory: LoadBalancerClientFactory,
        transformers: List<LoadBalancerFeignRequestTransformer>,
    ): Client = FeignBlockingLoadBalancerClient(FeignOkHttpClient(), loadBalancerClient, loadBalancerClientFactory, transformers)
}
