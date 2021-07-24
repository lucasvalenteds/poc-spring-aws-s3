package com.example.document;

import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.nio.file.Path;
import java.util.UUID;

public final class DocumentRepository {

    private final String bucket;
    private final S3AsyncClient client;
    private final S3Presigner signer;

    public DocumentRepository(String bucket, S3AsyncClient client, S3Presigner signer) {
        this.bucket = bucket;
        this.client = client;
        this.signer = signer;
    }

    public Mono<Document> create(UUID ownerId, Path path, MediaType mediaType) {
        var documentKey = Mono.from(createDocumentKey(ownerId, mediaType));
        var requestBody = Mono.from(createRequestBodyWithFile(path));
        var contentType = Mono.just(mediaType.toString());
        var contentLength = Mono.fromCallable(() -> path.toFile().length());

        return Mono.zip(documentKey, requestBody, contentType, contentLength)
            .flatMap(tuple -> {
                var objectRequest = PutObjectRequest.builder()
                    .bucket(this.bucket)
                    .key(tuple.getT1())
                    .contentType(tuple.getT3())
                    .contentLength(tuple.getT4())
                    .build();

                return Mono.fromFuture(() -> client.putObject(objectRequest, tuple.getT2()))
                    .then(Mono.just(Document.withURL(tuple.getT1())));
            });
    }

    private Mono<UUID> createRandomUUID() {
        return Mono.fromCallable(UUID::randomUUID)
            .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<String> createDocumentKey(UUID owner, MediaType mediaType) {
        var ownerId = Mono.just(owner);

        var filename = Mono.zip(
            Mono.from(createRandomUUID())
                .map(UUID::toString),
            Mono.justOrEmpty(mediaType.getSubtype()),
            (documentId, extension) -> documentId + "." + extension);

        return Mono.zip(ownerId, filename)
            .map(tuple -> String.format("example-service/uploads/%s/%s", tuple.getT1(), tuple.getT2()));
    }

    private Mono<AsyncRequestBody> createRequestBodyWithFile(Path path) {
        return Mono.fromCallable(() -> AsyncRequestBody.fromFile(path))
            .subscribeOn(Schedulers.boundedElastic());
    }
}
