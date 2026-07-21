package com.team1.project_lab_backend.identity.resolvers

import com.team1.project_lab_backend.identity.dto.HostRequest
import com.team1.project_lab_backend.identity.models.Host
import com.team1.project_lab_backend.identity.services.HostService
import com.team1.project_lab_backend.util.requireAuthenticated
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.math.BigDecimal

@Controller
class HostResolver(private val hostService: HostService) {
    @QueryMapping
    suspend fun hosts(): List<Host> = hostService.getAllHosts()

    @QueryMapping
    suspend fun host(
        @Argument id: Int,
    ): Host = hostService.getHostById(id)

    @MutationMapping
    suspend fun createHost(
        @Argument input: CreateHostInput,
    ): Host {
        requireAuthenticated()
        return hostService.createHost(input.toRequest())
    }

    @MutationMapping
    suspend fun updateHost(
        @Argument id: Int,
        @Argument input: UpdateHostInput,
    ): Host {
        requireAuthenticated()
        return hostService.updateHost(id, input.toRequest(id))
    }

    @MutationMapping
    suspend fun deleteHost(
        @Argument id: Int,
    ): Boolean {
        requireAuthenticated()
        hostService.deleteHost(id)
        return true
    }
}

data class CreateHostInput(
    val id: Int,
    val communicationRating: BigDecimal? = null,
    val checkinProcessRating: BigDecimal? = null,
    val cancellationRate: BigDecimal? = null,
    val languageIds: Set<Int> = emptySet(),
) {
    fun toRequest() =
        HostRequest(
            id = id,
            communicationRating = communicationRating,
            checkinProcessRating = checkinProcessRating,
            cancellationRate = cancellationRate,
            languageIds = languageIds,
        )
}

data class UpdateHostInput(
    val communicationRating: BigDecimal? = null,
    val checkinProcessRating: BigDecimal? = null,
    val cancellationRate: BigDecimal? = null,
    val languageIds: Set<Int> = emptySet(),
) {
    fun toRequest(id: Int) =
        HostRequest(
            id = id,
            communicationRating = communicationRating,
            checkinProcessRating = checkinProcessRating,
            cancellationRate = cancellationRate,
            languageIds = languageIds,
        )
}
