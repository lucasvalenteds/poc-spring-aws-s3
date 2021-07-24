package com.example.document;

import org.springframework.http.MediaType;

import java.nio.file.Path;
import java.util.UUID;

public final class DocumentTestBuilder {

    public static final UUID IMAGE_OWNER_ID = UUID.randomUUID();
    public static final Path IMAGE_PATH = Path.of("src", "test", "resources", "pepper.jpeg");
    public static final MediaType IMAGE_MEDIA_TYPE = MediaType.IMAGE_JPEG;

    private DocumentTestBuilder() {
    }
}
