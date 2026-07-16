package com.team1.project_lab_backend.inventory.config

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
 * not be empty". Confirmed live in gateway (see its copy of this class for the full
 * incident writeup); applied here proactively since this module's own Feign clients
 * (booking-service, identity-service) are exposed to the exact same upstream bug.
 *
 * Forcing one eager, single-threaded `getConverters()` call per registered Feign client
 * context here — during this bean's own construction, before the app accepts real
 * traffic — populates every list deterministically ahead of any concurrent access.
 * Enumerated generically via the `FeignClientSpecification` beans Spring Cloud OpenFeign
 * registers for every `@FeignClient` it discovers, rather than hardcoding context
 * names/ids — new Feign clients are covered automatically.
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
