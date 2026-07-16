package com.team1.project_lab_backend.identity.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.math.BigDecimal

@Entity
@Table(name = "host")
open class Host(
    @Id
    @Column(name = "id")
    open val id: Int,
    @Column(name = "communication_rating", precision = 4, scale = 1)
    @field:DecimalMin("0.0")
    @field:DecimalMax("100.0")
    open val communicationRating: BigDecimal? = null,
    @Column(name = "checkin_process_rating", precision = 4, scale = 1)
    @field:DecimalMin("0.0")
    @field:DecimalMax("100.0")
    open val checkinProcessRating: BigDecimal? = null,
    @Column(name = "cancellation_rate", precision = 4, scale = 1)
    @field:DecimalMin("0.0")
    @field:DecimalMax("100.0")
    open val cancellationRate: BigDecimal? = null,
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "host_language",
        joinColumns = [JoinColumn(name = "host_id")],
        inverseJoinColumns = [JoinColumn(name = "language_id")],
    )
    @OnDelete(action = OnDeleteAction.CASCADE)
    open val languages: MutableSet<Language> = mutableSetOf(),
)
