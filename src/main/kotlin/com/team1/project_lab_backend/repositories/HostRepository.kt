package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.Host
import org.springframework.data.jpa.repository.JpaRepository

interface HostRepository : JpaRepository<Host, Int>
