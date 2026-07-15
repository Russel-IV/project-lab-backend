package com.team1.project_lab_backend.inventory.repositories

import com.team1.project_lab_backend.inventory.models.Address
import org.springframework.data.jpa.repository.JpaRepository

interface AddressRepository : JpaRepository<Address, Int>
