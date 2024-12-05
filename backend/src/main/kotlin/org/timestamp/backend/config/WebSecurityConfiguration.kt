package org.timestamp.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf {
            it.disable() // Don't need cross site request forgery protection
        }.authorizeHttpRequests { auth ->
            auth.requestMatchers("/.well-known/**", "/", "/index.html", "/test/**")
                .permitAll()
                .anyRequest()
                .authenticated()
        }.addFilterBefore(
            FirebaseAuthFilter(),
            UsernamePasswordAuthenticationFilter::class.java
        )

        return http.build()
    }
}
