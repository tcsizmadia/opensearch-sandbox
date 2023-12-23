package com.github.tcsizmadia.opensearchsandbox.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile("!test")
public class SandboxCommandLineRunner implements CommandLineRunner {
    private final OpenSearchService openSearchService;
    private static final Logger logger = LoggerFactory.getLogger(SandboxCommandLineRunner.class);

    public SandboxCommandLineRunner(OpenSearchService openSearchService) {
        this.openSearchService = openSearchService;
    }

    @Override
    public void run(String... args) throws IOException {
        var response = openSearchService.ping();
        logger.info("Ping response: {}", response);
    }
}
