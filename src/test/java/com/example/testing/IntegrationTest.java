package com.example.testing;

import com.example.AppConfiguration;
import com.example.S3Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig({S3Configuration.class, AppConfiguration.class})
@Testcontainers
public abstract class IntegrationTest {

    private static final String IMAGE = "localstack/localstack:0.12.15";

    @Container
    private static final LocalStackContainer CONTAINER = new LocalStackContainer(DockerImageName.parse(IMAGE))
        .withServices(LocalStackContainer.Service.S3);

    @DynamicPropertySource
    private static void setApplicationProperties(DynamicPropertyRegistry registry) {
        registry.add("aws.accessKey", CONTAINER::getAccessKey);
        registry.add("aws.secretKey", CONTAINER::getSecretKey);
        registry.add("aws.region", CONTAINER::getRegion);
        registry.add("aws.url", () -> CONTAINER.getEndpointOverride(LocalStackContainer.Service.S3));
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
