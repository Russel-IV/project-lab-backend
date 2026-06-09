package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.Booking
import org.springframework.data.jpa.repository.JpaRepository

interface BookingRepository : JpaRepository<Booking, Int>
