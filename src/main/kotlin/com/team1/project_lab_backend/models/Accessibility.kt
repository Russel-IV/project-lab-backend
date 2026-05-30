package com.team1.project_lab_backend.models

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(
    name = "accessibility",
    uniqueConstraints = [UniqueConstraint(columnNames = ["accessibility_type"])]
)
open class Accessibility(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,

    @Column(name = "accessibility_type", nullable = false, unique = true, length = 150)
    @field:NotBlank
    @field:Size(max = 150)
    open val accessibilityType: String
)
