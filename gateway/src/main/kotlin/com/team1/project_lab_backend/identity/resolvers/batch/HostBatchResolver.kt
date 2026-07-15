package com.team1.project_lab_backend.identity.resolvers.batch

import com.team1.project_lab_backend.identity.models.Host
import com.team1.project_lab_backend.identity.models.Language
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller

@Controller
class HostBatchResolver {
    @BatchMapping
    fun languages(hosts: List<Host>): Map<Host, List<Language>> = hosts.associateWith { it.languages }
}
