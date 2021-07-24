package com.example.document;

import com.example.testing.IntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
class DocumentControllerTest extends IntegrationTest {

    @Container
    private static final LocalStackContainer CONTAINER = new LocalStackContainer(DockerImageName.parse(IMAGE))
        .withServices(LocalStackContainer.Service.S3);

    @DynamicPropertySource
    private static void setApplicationProperties(DynamicPropertyRegistry registry) {
        IntegrationTest.setApplicationProperties(registry, CONTAINER);
    }

    private final UUID ownerId = UUID.randomUUID();

    private Path uploadsDirectory;
    private WebTestClient client;

    @BeforeEach
    public void beforeEach(ApplicationContext context) throws IOException {
        var repository = context.getBean(DocumentRepository.class);

        this.uploadsDirectory = context.getBean("uploads", Path.class);
        this.client = WebTestClient.bindToController(new DocumentController(repository, uploadsDirectory))
            .build();

        Files.createDirectory(this.uploadsDirectory);
    }

    @AfterEach
    public void afterEach() throws IOException {
        Files.delete(uploadsDirectory);
    }

    @Test
    void testPersistingFile() {
        var file = new FileSystemResource(Path.of("src", "test", "resources", "pepper.jpeg"));

        var document = client.post()
            .uri("/documents/owners/{ownerId}", ownerId)
            .contentType(MediaType.IMAGE_JPEG)
            .body(BodyInserters.fromMultipartData("file", file))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CREATED)
            .expectBody(Document.class)
            .returnResult().getResponseBody();

        assertNotNull(document);
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
    }
}
