package com.cts.agrilink.config;

import com.cts.agrilink.security.JwtAuthFilter;
import org.springframework.boot.jpa.autoconfigure.EntityManagerFactoryBuilderCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterRegistrationConfig {

    /**
     * Prevents JwtAuthFilter from auto-registering as a global servlet filter.
     * Each SecurityFilterChain adds it explicitly via addFilterBefore(),
     * which is the correct Spring Security pattern.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> iamJwtFilterRegistration(JwtAuthFilter filter) {
        FilterRegistrationBean<JwtAuthFilter> bean = new FilterRegistrationBean<>(filter);
        bean.setEnabled(false);
        return bean;
    }

    /**
     * Registers produce-module entity classes with the JPA persistence unit.
     * Uses PersistenceUnitPostProcessor since @EntityScan was removed in Spring Boot 4.
     */
    @Bean
    public EntityManagerFactoryBuilderCustomizer produceModuleEntityScan() {
        return builder -> builder.setPersistenceUnitPostProcessors(pui -> {
            pui.addManagedClassName("com.agrilink.produce_module.entity.ProduceListing");
            pui.addManagedClassName("com.agrilink.produce_module.entity.ProduceSale");
        });
    }
}
