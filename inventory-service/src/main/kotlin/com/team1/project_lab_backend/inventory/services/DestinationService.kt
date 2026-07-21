package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.models.Destination
import com.team1.project_lab_backend.inventory.repositories.AddressRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DestinationService(
    private val addressRepository: AddressRepository,
) {
    @Transactional(readOnly = true)
    fun getAllDestinations(): List<Destination> =
        addressRepository.findDistinctCityCountryPairs()
            .map { Destination(city = it.city, countryCode = it.countryCode) }
}
