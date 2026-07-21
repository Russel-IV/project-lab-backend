package com.team1.project_lab_backend.inventory.services

import com.team1.project_lab_backend.inventory.models.Destination
import org.springframework.stereotype.Service

/**
 * Orchestration shim (docs/adr/0005): destination data lives in inventory-service,
 * reached via destinationFeignClient.
 */
@Service
class DestinationService(private val destinationFeignClient: DestinationFeignClient) {
    fun getAllDestinations(): List<Destination> = destinationFeignClient.list()
}
