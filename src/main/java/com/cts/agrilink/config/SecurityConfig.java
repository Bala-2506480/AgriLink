package com.cts.agrilink.config;

import com.cts.agrilink.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers("/agriLink/session/login", "/agriLink/session/refresh")
                        .permitAll()

                        // User & Role
                        .requestMatchers(HttpMethod.POST, "/agriLink/user/createUser")
                        .hasAnyRole("AgriLinkAdmin", "ExtensionOfficer")

                        .requestMatchers("/agriLink/user/**", "/agriLink/role/**")
                        .hasRole("AgriLinkAdmin")

                        // ✅ Farmer creates own profile
                        .requestMatchers(HttpMethod.POST,
                                "/agriLink/farmerLandRegistration/farmer/createFarmer")
                        .hasRole("Farmer")

                        // ✅ Land create allowed for Admin + Farmer
                        .requestMatchers(HttpMethod.POST,
                                "/agriLink/farmerLandRegistration/landHolding/createLandHolding")
                        .hasAnyRole("AgriLinkAdmin", "Farmer")

                        // Delete only Admin
                        .requestMatchers(HttpMethod.DELETE,
                                "/agriLink/farmerLandRegistration/**")
                        .hasRole("AgriLinkAdmin")

                        // Read + update
                        .requestMatchers("/agriLink/farmerLandRegistration/**")
                        .hasAnyRole("AgriLinkAdmin", "Farmer")

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
