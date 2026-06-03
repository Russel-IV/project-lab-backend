package com.team1.project_lab_backend.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class TestController {

    @GetMapping("/")
    fun healthCheck(): Map<String, String> {
        return mapOf(
            "status" to "UP",
            "message" to "Service is running successfully!"
        )
    }

    @GetMapping("/api/hello")
    fun helloWorld(): Map<String, String> {
        return mapOf(
            "message" to "Hello World!",
            "status" to "success",
            "timestamp" to Instant.now().toString()
        )
    }
}
