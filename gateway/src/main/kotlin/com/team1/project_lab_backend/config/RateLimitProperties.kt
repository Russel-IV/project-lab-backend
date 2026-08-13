package com.team1.project_lab_backend.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.rate-limit")
data class RateLimitProperties(
    val enabled: Boolean = true,
    val requestsPerMinute: Long = 120,
    val burstCapacity: Long = 120,
)
