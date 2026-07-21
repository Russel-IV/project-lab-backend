package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.models.Destination
import com.team1.project_lab_backend.inventory.repositories.RegionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DestinationService(
    private val regionRepository: RegionRepository,
) {
    @Transactional(readOnly = true)
    fun searchDestinations(
        search: String?,
        limit: Int,
    ): List<Destination> =
        regionRepository.search(search, limit)
            .map { Destination(city = it.city, countryCode = it.countryCode, regionId = it.id) }
}
