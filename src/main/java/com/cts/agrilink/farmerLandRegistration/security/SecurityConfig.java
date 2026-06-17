package com.cts.agrilink.farmerLandRegistration.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // Public
                        .requestMatchers("/auth/register", "/auth/login").permitAll()

                        // ADMIN only
                        .requestMatchers(HttpMethod.POST,   "/farmerLandRegistration/farmer/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST,   "/farmerLandRegistration/landHolding/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/farmerLandRegistration/**").hasRole("ADMIN")
                        .requestMatchers("/farmerLandRegistration/updateUser/**").hasRole("ADMIN")
                        .requestMatchers("/farmerLandRegistration/deleteUser/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/farmerLandRegistration/farmer/fetchFarmers").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/farmerLandRegistration/landHolding/fetchLandHoldings").hasRole("ADMIN")

                        // ADMIN or FARMER
                        .requestMatchers(HttpMethod.GET, "/farmerLandRegistration/**").hasAnyRole("ADMIN", "FARMER")
                        .requestMatchers(HttpMethod.PUT, "/farmerLandRegistration/**").hasAnyRole("ADMIN", "FARMER")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}