package com.example.document;

import com.example.testing.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.test.StepVerifier;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
class DocumentRepositoryTest extends IntegrationTest {

    @Container
    private static final LocalStackContainer CONTAINER = new LocalStackContainer(DockerImageName.parse(IMAGE))
        .withServices(LocalStackContainer.Service.S3);

    @DynamicPropertySource
    private static void setApplicationProperties(DynamicPropertyRegistry registry) {
        IntegrationTest.setApplicationProperties(registry, CONTAINER);
    }

    private String bucket;
    private URI endpoint;
    private DocumentRepository repository;

    @BeforeEach
    public void beforeEach(ApplicationContext context) {
        this.bucket = context.getBean("bucket", String.class);
        this.endpoint = context.getBean("endpoint", URI.class);
        this.repository = context.getBean(DocumentRepository.class);
    }

    @Test
    void testCreatingDocument() {
        var path = DocumentTestBuilder.IMAGE_PATH;
        var mediaType = DocumentTestBuilder.IMAGE_MEDIA_TYPE;
        var ownerId = DocumentTestBuilder.IMAGE_OWNER_ID;

        StepVerifier.create(repository.create(ownerId, path, mediaType))
            .assertNext(document -> {
                assertNotNull(document.getUrl());

                var url = Arrays.stream(document.getUrl().split("/")).iterator();
                assertEquals("example-service", url.next());
                assertEquals("uploads", url.next());
                assertEquals(ownerId.toString(), url.next());

                var filename = Arrays.stream(url.next().split("\\.")).iterator();
                assertDoesNotThrow(() -> UUID.fromString(filename.next()));
                assertEquals("jpeg", filename.next());

                assertFalse(url.hasNext());
                assertFalse(filename.hasNext());
            })
            .verifyComplete();
    }

    @Test
    void testCreatingTemporaryURL() {
        var path = DocumentTestBuilder.IMAGE_PATH;
        var mediaType = DocumentTestBuilder.IMAGE_MEDIA_TYPE;

        var ownerId = DocumentTestBuilder.IMAGE_OWNER_ID;
        var filename = repository.create(ownerId, path, mediaType)
            .map(document -> Path.of(document.getUrl()).getFileName().toString())
            .block();

        assertNotNull(filename);

        StepVerifier.create(repository.generateTemporaryURL(ownerId, filename))
            .assertNext(document ->
                assertThat(document.getUrl())
                    .isNotNull()
                    .isNotBlank()
                    .containsSubsequence(List.of(
                        endpoint.toString(),
                        bucket,
                        "example-service/uploads",
                        ownerId.toString(),
                        filename
                    ))
                    .contains(List.of(
                        "?X-Amz-Algorithm=AWS4-HMAC-SHA256",
                        "&X-Amz-Date=",
                        "&X-Amz-SignedHeaders=host",
                        "&X-Amz-Expires=86400",
                        "&X-Amz-Credential=",
                        "&X-Amz-Signature="
                    ))
            )
            .verifyComplete();
    }
}
