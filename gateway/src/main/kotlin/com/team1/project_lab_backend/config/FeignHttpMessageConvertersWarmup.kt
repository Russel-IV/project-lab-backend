package com.team1.project_lab_backend.config

import org.springframework.cloud.openfeign.FeignClientFactory
import org.springframework.cloud.openfeign.FeignClientSpecification
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters
import org.springframework.stereotype.Component

/**
 * spring-cloud-openfeign-core 5.0.2's FeignHttpMessageConverters.initConvertersIfRequired()
 * (read from its sources) has a genuine thread-safety bug: it assigns
 * `this.converters = new ArrayList<>()` — a non-null but still-EMPTY list — *before*
 * populating it, with no synchronization or volatile guard. Each `@FeignClient` gets its
 * own isolated child ApplicationContext (keyed by `contextId` if set, else `name`/`value`),
 * and therefore its own independent `FeignHttpMessageConverters` instance. If two threads
 * race to decode a response through the *same* Feign client for the first time, the second
 * thread's null-check can observe the still-empty list and return it as-is, skipping
 * population entirely — Feign's `SpringDecoder` then fails with "'messageConverters' must
 * not be empty". Confirmed live: first seen when two near-simultaneous GraphQL requests
 * both decoded a `MediaFeignClient` response for the first time; reproduced again later
 * under concurrent load (~35% of a 20-request burst) against a build that predated this
 * fix — a real, non-rare race under concurrency, not a one-off fluke, and not a bug in this
 * app's own Feign clients. Verified after this fix: 150/150 concurrent requests clean.
 *
 * Forcing one eager, single-threaded `getConverters()` call per registered Feign client
 * context here — during this bean's own construction, before the app accepts real
 * traffic — populates every list deterministically ahead of any concurrent access,
 * closing the race window for every current and future `@FeignClient` in this module.
 * Enumerated generically via the `FeignClientSpecification` beans Spring Cloud OpenFeign
 * registers for every `@FeignClient` it discovers (one per client, unconditionally,
 * regardless of whether a custom `configuration=` was given) rather than hardcoding the
 * ~16 context names/ids this module currently has — new Feign clients are covered
 * automatically.
 */
@Component
class FeignHttpMessageConvertersWarmup(
    feignClientFactory: FeignClientFactory,
    specifications: List<FeignClientSpecification>,
) {
    init {
        specifications
            .map { it.name }
            .distinct()
            .forEach { name ->
                feignClientFactory.getInstance(name, FeignHttpMessageConverters::class.java)?.converters
            }
    }
}
