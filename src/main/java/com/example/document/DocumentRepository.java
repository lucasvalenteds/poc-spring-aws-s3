package com.example.document;

import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public final class DocumentRepository {

    private final String bucket;
    private final S3AsyncClient client;
    private final S3Presigner signer;

    public DocumentRepository(String bucket, S3AsyncClient client, S3Presigner signer) {
        this.bucket = bucket;
        this.client = client;
        this.signer = signer;
    }
}
