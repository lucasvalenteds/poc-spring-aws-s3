package com.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;

@EnableWebFlux
@ComponentScan("com.example.spring")
@PropertySource("classpath:application.properties")
public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        var context = new AnnotationConfigApplicationContext(Main.class);

        var environment = context.getEnvironment();

        HttpServer.create()
                .port(environment.getRequiredProperty("server.port", Integer.class))
                .handle(new ReactorHttpHandlerAdapter(WebHttpHandlerBuilder.applicationContext(context).build()))
                .bindUntilJavaShutdown(
                        Duration.ofMillis(1000),
                        server -> logger.info("Server running on port {}", server.port())
                );
    }
}
