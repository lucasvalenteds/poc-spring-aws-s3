package com.example;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@PropertySource("classpath:application.properties")
public class S3Configuration {

    @Bean
    AwsCredentialsProvider credentialsProvider(Environment environment) {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(
            environment.getRequiredProperty("aws.accessKey", String.class),
            environment.getRequiredProperty("aws.secretKey", String.class)
        ));
    }

    @Bean("endpoint")
    URI endpoint(Environment environment) {
        return URI.create(environment.getRequiredProperty("aws.url", String.class));
    }

    @Bean
    Region region(Environment environment) {
        return Region.of(environment.getRequiredProperty("aws.region", String.class));
    }

    @Bean("bucket")
    String bucket(Environment environment) {
        return environment.getRequiredProperty("aws.s3.bucket", String.class);
    }

    @Bean
    S3AsyncClient client(AwsCredentialsProvider credentialsProvider, URI endpoint, Region region) {
        return S3AsyncClient.builder()
            .credentialsProvider(credentialsProvider)
            .endpointOverride(endpoint)
            .region(region)
            .build();
    }

    @Bean
    S3Presigner signer(AwsCredentialsProvider credentialsProvider, URI endpoint, Region region) {
        return S3Presigner.builder()
            .credentialsProvider(credentialsProvider)
            .endpointOverride(endpoint)
            .region(region)
            .build();
    }
}
