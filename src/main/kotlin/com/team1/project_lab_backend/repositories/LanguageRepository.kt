package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.Language
import org.springframework.data.jpa.repository.JpaRepository

interface LanguageRepository : JpaRepository<Language, Int>
