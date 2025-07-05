package io.graversen.starter.spring.confluence.rag;

import feign.Feign;
import feign.RequestInterceptor;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class ConfluenceConfiguration {
    @Bean
    public ConfluenceApi confluencePageClient(SpringConfluenceRagProperties springConfluenceRagProperties) {
        final var confluenceProperties = springConfluenceRagProperties.getConfluence();
        return Feign.builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .requestInterceptor(confluenceAuthenticator(springConfluenceRagProperties))
                .target(ConfluenceApi.class, confluenceProperties.getBaseUrl());
    }

    private RequestInterceptor confluenceAuthenticator(SpringConfluenceRagProperties springConfluenceRagProperties) {
        return template -> {
            final var confluenceProperties = springConfluenceRagProperties.getConfluence();
            final var auth = confluenceProperties.getUsername() + ":" + confluenceProperties.getApiKey();
            final var base64 = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            template.header("Authorization", "Basic " + base64);
        };
    }
}
