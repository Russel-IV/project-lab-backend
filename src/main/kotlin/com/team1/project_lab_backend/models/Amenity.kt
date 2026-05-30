package com.team1.project_lab_backend.models

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

enum class AmenityType {
    ROOM_AMENITY, PROPERTY_AMENITY
}

@Entity
@Table(
    name = "amenity",
    uniqueConstraints = [UniqueConstraint(columnNames = ["name"])]
)
open class Amenity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,

    @Column(name = "name", nullable = false, unique = true, length = 150)
    @field:NotBlank
    @field:Size(max = 150)
    open val name: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @field:NotNull
    open val type: AmenityType
)
