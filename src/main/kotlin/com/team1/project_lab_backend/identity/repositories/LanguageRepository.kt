package com.team1.project_lab_backend.identity.repositories

import com.team1.project_lab_backend.identity.models.Language
import org.springframework.data.jpa.repository.JpaRepository

interface LanguageRepository : JpaRepository<Language, Int>
