package com.team1.project_lab_backend.resolvers.batch

import com.team1.project_lab_backend.models.Host
import com.team1.project_lab_backend.models.Language
import com.team1.project_lab_backend.repositories.HostRepository
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller

@Controller
class HostBatchResolver(
    private val hostRepository: HostRepository,
) {
    @BatchMapping
    fun languages(hosts: List<Host>): Map<Host, List<Language>> {
        val ids = hosts.map { it.id }
        val loaded = hostRepository.findByIdInWithLanguages(ids).associateBy { it.id }
        return hosts.associateWith { loaded[it.id]?.languages?.toList() ?: emptyList() }
    }
}
