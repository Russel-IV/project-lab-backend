package com.team1.project_lab_backend.models

import jakarta.persistence.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Entity
@Table(name = "user")
open class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,

    @Column(name = "name", nullable = false, length = 255)
    @field:NotBlank
    @field:Size(max = 255)
    open val name: String
)
