package com.team1.project_lab_backend.config

import jakarta.servlet.MultipartConfigElement
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Path

@Configuration
class WebConfig(
    @Value("\${app.upload.dir}") private val uploadDir: String
) : WebMvcConfigurer {
    // This Spring Boot version moved MultipartAutoConfiguration into a separate
    // spring-boot-servlet artifact (transitively present, confirmed via `jar tf` on
    // the built fat jar), but the DispatcherServlet registration this app actually
    // gets never picks up its MultipartConfigElement bean — file uploads fail with
    // "Unable to process parts as no multi-part configuration has been provided"
    // without this explicit bean.
    @Bean
    fun multipartConfigElement(): MultipartConfigElement = MultipartConfigElement("")

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        val absolutePath = Path.of(uploadDir).toAbsolutePath().toString()
        registry.addResourceHandler("/uploads/**")
            .addResourceLocations("file:$absolutePath/")
    }

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(object : HandlerInterceptor {
            override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
                if (request.requestURI.startsWith("/uploads/")) {
                    response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment")
                }
                return true
            }
        })
    }
}
