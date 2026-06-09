package com.team1.project_lab_backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path

@Configuration
class WebConfig(
    @Value("\${app.upload.dir}") private val uploadDir: String
) : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val absolutePath = Path.of(uploadDir).toAbsolutePath().toString()
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:$absolutePath/")
    }
}
