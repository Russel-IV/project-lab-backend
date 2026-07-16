package com.team1.project_lab_backend.identity.dto

import java.math.BigDecimal

data class HostRequest(
    val id: Int? = null,
    val communicationRating: BigDecimal? = null,
    val checkinProcessRating: BigDecimal? = null,
    val cancellationRate: BigDecimal? = null,
    val languageIds: Set<Int> = emptySet(),
)
