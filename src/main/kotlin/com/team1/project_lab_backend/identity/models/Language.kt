package com.team1.project_lab_backend.identity.models

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(
    name = "language",
    uniqueConstraints = [UniqueConstraint(columnNames = ["language_name"])]
)
open class Language(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,

    @Column(name = "language_name", nullable = false, unique = true, length = 100)
    @field:NotBlank
    @field:Size(max = 100)
    open val languageName: String
)
