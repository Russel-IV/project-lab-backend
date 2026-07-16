package com.team1.project_lab_backend.identity.models

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

@Entity
@Table(name = "\"user\"")
open class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Int = 0,
    @Column(name = "name", nullable = false, length = 255)
    @field:NotBlank
    @field:Size(max = 255)
    open val name: String,
    @Column(name = "email", unique = true, length = 320)
    open val email: String? = null,
    @JsonIgnore
    @Column(name = "password_hash")
    open val passwordHash: String? = null,
    @Column(name = "phone", length = 32)
    open val phone: String? = null,
    @Column(name = "profile_picture_url")
    open val profilePictureUrl: String? = null,
    @Column(name = "deleted_at")
    open val deletedAt: LocalDateTime? = null,
)
