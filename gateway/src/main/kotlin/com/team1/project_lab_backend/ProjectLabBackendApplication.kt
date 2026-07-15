package com.team1.project_lab_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.modulith.Modulithic

@SpringBootApplication
@Modulithic(sharedModules = ["config", "util"])
class ProjectLabBackendApplication

fun main(args: Array<String>) {
    runApplication <ProjectLabBackendApplication>(*args)
}
