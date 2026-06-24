package com.cts.agrilink.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @ConditionalOnClass(name = "org.springframework.web.reactive.function.client.WebClient")
    public WebClient iamWebClient(ObjectProvider<WebClient.Builder> builderProvider,
                                  @Value("${iam.base-url:http://localhost:8089}") String baseUrl) {
        try {
            WebClient.Builder builder = builderProvider == null ? null : builderProvider.getIfAvailable();
            if (builder == null) {
                builder = WebClient.builder();
            }
            return builder.baseUrl(baseUrl).build();
        } catch (NoClassDefFoundError | Exception ex) {
            // WebClient not on classpath or failed to create — no bean created
            return null;
        }
    }

}
