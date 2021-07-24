package com.example.document;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentRepository repository;
    private final Path uploadsDirectory;

    public DocumentController(DocumentRepository repository, Path uploadsDirectory) {
        this.repository = repository;
        this.uploadsDirectory = uploadsDirectory;
    }

    @PostMapping(
        path = "/owners/{ownerId}",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Document> create(@PathVariable UUID ownerId, @RequestPart("file") Mono<FilePart> filePart) {
        var owner = Mono.just(ownerId);

        var filename = Mono.from(filePart)
            .map(part -> uploadsDirectory.resolve(part.filename()));

        var mediaType = Mono.from(filePart)
            .map(part -> part.headers().getContentType());

        return Mono.zip(owner, filePart, filename, mediaType)
            .delayUntil(tuple -> tuple.getT2().transferTo(tuple.getT3()))
            .flatMap(tuple -> repository.create(tuple.getT1(), tuple.getT3(), tuple.getT4()))
            .delayUntil(tuple -> deleteFileFromFileSystem(filename));
    }

    private Mono<Void> deleteFileFromFileSystem(Mono<Path> path) {
        return Mono.from(path)
            .flatMap(filename ->
                Mono.fromCallable(() -> {
                    Files.delete(filename);
                    return null;
                })
            )
            .subscribeOn(Schedulers.boundedElastic())
            .then();
    }
}
