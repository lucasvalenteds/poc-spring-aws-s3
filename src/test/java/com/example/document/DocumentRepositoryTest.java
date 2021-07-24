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

import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

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

    private final UUID ownerId = UUID.randomUUID();

    private DocumentRepository repository;

    @BeforeEach
    public void beforeEach(ApplicationContext context) {
        this.repository = context.getBean(DocumentRepository.class);
    }

    @Test
    void testPersistingFile() {
        var path = Path.of("src", "test", "resources", "pepper.jpeg");
        var mediaType = MediaType.IMAGE_JPEG;

        StepVerifier.create(repository.persist(ownerId, path, mediaType))
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
}
