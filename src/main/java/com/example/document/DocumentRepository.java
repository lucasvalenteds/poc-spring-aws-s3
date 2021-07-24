package com.example.document;

import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

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

    public Mono<Document> persist(UUID ownerId, MultipartFile file) {
        var documentKey = Mono.from(createDocumentKey(ownerId, file));
        var requestBody = Mono.from(createRequestBodyWithFile(file));
        var contentType = Mono.justOrEmpty(file.getContentType());
        var contentLength = Mono.fromCallable(() -> file.getResource().contentLength());

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

    private Mono<String> createDocumentKey(UUID owner, MultipartFile file) {
        var ownerId = Mono.just(owner);

        var filename = Mono.zip(
            Mono.from(createRandomUUID())
                .map(UUID::toString),
            Mono.justOrEmpty(file.getContentType())
                .map(MediaType::parseMediaType)
                .map(MimeType::getSubtype),
            (documentId, extension) -> documentId + "." + extension);

        return Mono.zip(ownerId, filename)
            .map(tuple -> String.format("example-service/uploads/%s/%s", tuple.getT1(), tuple.getT2()));
    }

    private Mono<AsyncRequestBody> createRequestBodyWithFile(MultipartFile file) {
        return Mono.fromCallable(() -> AsyncRequestBody.fromBytes(file.getBytes()))
            .subscribeOn(Schedulers.boundedElastic());
    }
}
