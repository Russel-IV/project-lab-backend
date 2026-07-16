package com.team1.project_lab_backend.inventory.models

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(
    name = "traveler_experience",
    uniqueConstraints = [UniqueConstraint(columnNames = ["traveler_experience_type"])],
)
open class TravelerExperience(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,
    @Column(name = "traveler_experience_type", nullable = false, unique = true, length = 100)
    @field:NotBlank
    @field:Size(max = 100)
    open val travelerExperienceType: String,
)
