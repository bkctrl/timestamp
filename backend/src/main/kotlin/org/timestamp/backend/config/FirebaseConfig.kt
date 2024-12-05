package org.timestamp.backend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import java.io.IOException

@Configuration
class FirebaseConfig {
    @Bean
    fun app(): FirebaseApp {
        val serviceAccount = this::class.java.classLoader
            .getResourceAsStream("firebase-admin-key.json")
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()
        return FirebaseApp.initializeApp(options)
    }
}

data class FirebaseUser(
    val uid: String,
    val email: String,
    val picture: String,
    val name: String,
    val emailVerified: Boolean
)

@Component
class FirebaseAuthFilter: jakarta.servlet.Filter {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(req: ServletRequest, res: ServletResponse, chain: FilterChain) {
        val httpReq = req as HttpServletRequest
        val auth = FirebaseAuth.getInstance()
        val authHeader = httpReq.getHeader("Authorization")

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.removePrefix("Bearer ")
                val decodedToken = auth.verifyIdToken(token)

                // This will be the token we can extract
                val firebasePrincipal = FirebaseUser(
                    decodedToken.uid,
                    decodedToken.email,
                    decodedToken.picture,
                    decodedToken.name,
                    decodedToken.isEmailVerified
                )

                // Inject the token into the security context
                val securityToken = UsernamePasswordAuthenticationToken(firebasePrincipal, null, emptyList())
                securityToken.details = WebAuthenticationDetailsSource().buildDetails(httpReq)
                SecurityContextHolder.getContext().authentication = securityToken
            }
        } catch (e: Exception) {
            println("Firebase token verification failed: ${e.message}")
        }

        chain.doFilter(req, res)
    }
}