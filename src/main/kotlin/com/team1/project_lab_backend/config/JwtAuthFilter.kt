package com.team1.project_lab_backend.config

import com.team1.project_lab_backend.repositories.UserRepository
import com.team1.project_lab_backend.services.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val token = header.removePrefix("Bearer ").trim()
            val userId = jwtService.extractUserId(token)
            if (userId != null && SecurityContextHolder.getContext().authentication == null) {
                userRepository.findById(userId)
                    .filter { it.deletedAt == null }
                    .ifPresent { user ->
                        val auth = UsernamePasswordAuthenticationToken(user, null, emptyList())
                        auth.details = WebAuthenticationDetailsSource().buildDetails(request)
                        SecurityContextHolder.getContext().authentication = auth
                    }
            }
        }
        filterChain.doFilter(request, response)
    }
}
