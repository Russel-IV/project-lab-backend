package com.team1.project_lab_backend.inventory.models

import jakarta.persistence.*
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

@Entity
@Table(name = "room")
open class Room(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,

    @Column(name = "stay_id", nullable = false)
    open val stayId: Int,

    @Column(name = "name", nullable = false, length = 255)
    open val name: String,

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @field:NotNull
    @field:DecimalMin("0.00")
    open val price: BigDecimal,

    @Column(name = "sleeps", nullable = false)
    @field:Min(1)
    open val sleeps: Int,

    @Column(name = "bedroom_amount", nullable = false)
    @field:Min(0)
    open val bedroomAmount: Int,

    @Column(name = "bathrooms", nullable = false, precision = 3, scale = 1)
    @field:NotNull
    @field:DecimalMin("0.0")
    open val bathrooms: BigDecimal,

    @Column(name = "size", precision = 10, scale = 1)
    @field:DecimalMin("0.0")
    open val size: BigDecimal? = null
)
