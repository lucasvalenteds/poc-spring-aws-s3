package com.example.testing;

import com.example.document.DocumentConfiguration;
import com.example.S3Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.localstack.LocalStackContainer;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig({S3Configuration.class, DocumentConfiguration.class})
public abstract class IntegrationTest {

    protected static final String IMAGE = "localstack/localstack:0.12.15";

    protected static void setApplicationProperties(DynamicPropertyRegistry registry, LocalStackContainer container) {
        registry.add("aws.accessKey", container::getAccessKey);
        registry.add("aws.secretKey", container::getSecretKey);
        registry.add("aws.region", container::getRegion);
        registry.add("aws.url", () -> container.getEndpointOverride(LocalStackContainer.Service.S3));
    }

    @BeforeAll
    public static void beforeAll(ApplicationContext context) {
        var bucket = context.getBean("bucket", String.class);
        var client = context.getBean(S3AsyncClient.class);

        var objectRequest = CreateBucketRequest.builder()
            .bucket(bucket)
            .build();

        Mono.fromFuture(() -> client.createBucket(objectRequest))
            .block();
    }
}
