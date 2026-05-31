package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.PaymentType
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentTypeRepository : JpaRepository<PaymentType, Int>
