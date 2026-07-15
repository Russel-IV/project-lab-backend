package com.team1.project_lab_backend.identity.models

import java.math.BigDecimal

/**
 * Owned by identity-service (docs/adr/0002, Phase 4) — this is just the GraphQL-facing
 * DTO shape, not a JPA entity. `languages` travels with every Host response identity-
 * service returns (it's a live field on its own Host entity), so HostBatchResolver's
 * @BatchMapping for Host.languages just reads it off the already-fetched Host — no
 * second Feign round trip needed.
 */
data class Host(
    val id: Int,
    val communicationRating: BigDecimal?,
    val checkinProcessRating: BigDecimal?,
    val cancellationRate: BigDecimal?,
    val languages: List<Language> = emptyList(),
)
