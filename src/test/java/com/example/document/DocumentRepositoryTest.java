package com.example.document;

import com.example.testing.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DocumentRepositoryTest extends IntegrationTest {

    private final UUID ownerId = UUID.randomUUID();

    private DocumentRepository repository;

    @BeforeEach
    public void beforeEach(ApplicationContext context) {
        this.repository = context.getBean(DocumentRepository.class);
    }

    @Test
    void testPersistingFile() throws IOException {
        var name = "pepper";
        var originalFilename = name + ".jpeg";
        var contentType = MediaType.IMAGE_JPEG_VALUE;
        var content = Files.readAllBytes(Path.of("src", "test", "resources", originalFilename));
        var file = new MockMultipartFile(name, originalFilename, contentType, content);

        StepVerifier.create(repository.persist(ownerId, file))
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
