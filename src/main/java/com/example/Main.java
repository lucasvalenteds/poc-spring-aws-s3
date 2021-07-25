package com.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.server.HttpServer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public final class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        var context = new AnnotationConfigApplicationContext(Main.class.getPackageName());

        var environment = context.getEnvironment();

        Mono.just(context.getBean("uploads", Path.class))
            .filterWhen(path -> Mono.fromCallable(() -> !Files.exists(path)))
            .flatMap(path -> Mono.fromCallable(() -> Files.createDirectory(path)))
            .doOnNext(path -> LOGGER.info("Directory created: {}", path))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();

        HttpServer.create()
            .port(environment.getRequiredProperty("server.port", Integer.class))
            .handle(new ReactorHttpHandlerAdapter(WebHttpHandlerBuilder.applicationContext(context).build()))
            .bindUntilJavaShutdown(
                Duration.ofMillis(1000),
                server -> LOGGER.info("Server running on port {}", server.port())
            );
    }
}
