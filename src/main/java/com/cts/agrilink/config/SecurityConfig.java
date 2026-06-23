package com.cts.agrilink.config;

import com.cts.agrilink.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/agriLink/session/login", "/agriLink/session/refresh").permitAll()
                // Create user: Admin (any role) or ExtensionOfficer (Farmers only — enforced in service)
                .requestMatchers(HttpMethod.POST, "/agriLink/user/createUser")
                    .hasAnyRole("AgriLinkAdmin", "ExtensionOfficer")
                // All other user management + role management is Admin only
                .requestMatchers("/agriLink/user/**", "/agriLink/role/**").hasRole("AgriLinkAdmin")

                // ── Produce Module ────────────────────────────────────────────
                // Create listing: Farmer only
                .requestMatchers(HttpMethod.POST, "/agriLink/produceSales/createListing/**")
                    .hasRole("Farmer")
                // Create sale: ProcurementOfficer or Admin
                .requestMatchers(HttpMethod.POST, "/agriLink/produceSales/createSale/**")
                    .hasAnyRole("ProcurementOfficer", "AgriLinkAdmin")
                // Read listings & sales: Farmer, ProcurementOfficer, Admin
                .requestMatchers(HttpMethod.GET, "/agriLink/produceSales/**")
                    .hasAnyRole("Farmer", "ProcurementOfficer", "AgriLinkAdmin")
                // Update listing: Farmer (own enforced in service) or Admin
                .requestMatchers(HttpMethod.PUT, "/agriLink/produceSales/updateListing/**",
                                                  "/agriLink/produceSales/updateListingStatus/**")
                    .hasAnyRole("Farmer", "AgriLinkAdmin")
                // Update sale & payment status: ProcurementOfficer or Admin
                .requestMatchers(HttpMethod.PUT, "/agriLink/produceSales/updateSale/**",
                                                  "/agriLink/produceSales/updatePaymentStatus/**")
                    .hasAnyRole("ProcurementOfficer", "AgriLinkAdmin")
                // Delete listing: Farmer (own enforced in service) or Admin
                .requestMatchers(HttpMethod.DELETE, "/agriLink/produceSales/deleteListing/**")
                    .hasAnyRole("Farmer", "AgriLinkAdmin")
                // Delete sale: ProcurementOfficer or Admin
                .requestMatchers(HttpMethod.DELETE, "/agriLink/produceSales/deleteSale/**")
                    .hasAnyRole("ProcurementOfficer", "AgriLinkAdmin")

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}