package com.team1.project_lab_backend.inventory.config

import org.springframework.cloud.openfeign.FeignClientFactory
import org.springframework.cloud.openfeign.FeignClientSpecification
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters
import org.springframework.stereotype.Component

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
