package com.cts.agrilink.config;

import java.lang.reflect.Method;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientConfig {

    @Bean
    @ConditionalOnClass(name = "org.springframework.web.reactive.function.client.WebClient")
    public Object iamWebClient(ObjectProvider<Object> builderProvider,
                               @Value("${iam.base-url:http://localhost:8089}") String baseUrl) {
        try {
            Class<?> webClientClass = Class.forName("org.springframework.web.reactive.function.client.WebClient");
            // Try to get a provided builder (type unknown here so use ObjectProvider<Object>)
            Object builder = builderProvider == null ? null : builderProvider.getIfAvailable();
            if (builder == null) {
                Method builderFactory = webClientClass.getMethod("builder");
                builder = builderFactory.invoke(null);
            }
            Method baseUrlMethod = builder.getClass().getMethod("baseUrl", String.class);
            Object configured = baseUrlMethod.invoke(builder, baseUrl);
            Method buildMethod = configured.getClass().getMethod("build");
            return buildMethod.invoke(configured);
        } catch (ClassNotFoundException ex) {
            // WebClient not on classpath — no bean created
            return null;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create WebClient", ex);
        }
    }

}
