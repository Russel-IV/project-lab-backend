package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.Address
import org.springframework.data.jpa.repository.JpaRepository

interface AddressRepository : JpaRepository<Address, Int>
