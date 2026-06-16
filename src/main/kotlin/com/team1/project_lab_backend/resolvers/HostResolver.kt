package com.team1.project_lab_backend.resolvers

import com.team1.project_lab_backend.dto.HostRequest
import com.team1.project_lab_backend.models.Host
import com.team1.project_lab_backend.services.HostService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.math.BigDecimal

@Controller
class HostResolver(private val hostService: HostService) {

    @QueryMapping
    fun hosts(): List<Host> = hostService.getAllHosts()

    @QueryMapping
    fun host(@Argument id: Int): Host? = hostService.getHostById(id)

    @MutationMapping
    fun createHost(@Argument input: CreateHostInput): Host =
        hostService.createHost(input.toRequest())

    @MutationMapping
    fun updateHost(@Argument id: Int, @Argument input: UpdateHostInput): Host =
        hostService.updateHost(id, input.toRequest(id))

    @MutationMapping
    fun deleteHost(@Argument id: Int): Boolean {
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
    fun toRequest() = HostRequest(
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
    fun toRequest(id: Int) = HostRequest(
        id = id,
        communicationRating = communicationRating,
        checkinProcessRating = checkinProcessRating,
        cancellationRate = cancellationRate,
        languageIds = languageIds,
    )
}
