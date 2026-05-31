package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.Review
import org.springframework.data.jpa.repository.JpaRepository

interface ReviewRepository : JpaRepository<Review, Int>
