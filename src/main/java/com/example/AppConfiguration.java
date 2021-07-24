package com.example;

import com.example.document.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.nio.file.Path;

@Configuration
public class AppConfiguration {

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper()
            .findAndRegisterModules();
    }

    @Bean("uploads")
    Path uploads(Environment environment) {
        return Path.of(environment.getRequiredProperty("server.uploads", String.class));
    }

    @Bean
    DocumentRepository documentRepository(String bucket, S3AsyncClient client, S3Presigner signer) {
        return new DocumentRepository(bucket, client, signer);
    }
}
