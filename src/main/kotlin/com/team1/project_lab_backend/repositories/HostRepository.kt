package com.team1.project_lab_backend.repositories

import com.team1.project_lab_backend.models.Host
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface HostRepository : JpaRepository<Host, Int> {

    @Query("SELECT DISTINCT h FROM Host h LEFT JOIN FETCH h.languages WHERE h.id IN :ids")
    fun findByIdInWithLanguages(ids: List<Int>): List<Host>
}
