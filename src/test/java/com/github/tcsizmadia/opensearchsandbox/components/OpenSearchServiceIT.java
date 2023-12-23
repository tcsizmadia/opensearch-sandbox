package com.github.tcsizmadia.opensearchsandbox.components;

import com.github.tcsizmadia.opensearchsandbox.entities.Person;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OpenSearchServiceIT {

    @Autowired
    private OpenSearchService openSearchService;

    public static final String DOCKER_IMAGE_NAME = "opensearchproject/opensearch";

    public static final String OPEN_SEARCH_VERSION = System.getProperty("opensearch.version", "1.3.0");
    public static final int EXPOSED_PORT = 9200;
    @Container
    public static GenericContainer<?> opensearchContainer;
    @BeforeAll
    static void beforeAll() {
        opensearchContainer =
                new GenericContainer<>(DOCKER_IMAGE_NAME + ":" + OPEN_SEARCH_VERSION)
                        .withExposedPorts(EXPOSED_PORT)
                        .withEnv("discovery.type", "single-node");

        if (OPEN_SEARCH_VERSION.equals("1.0.0")) {
            opensearchContainer.waitingFor(Wait.forLogMessage(".*(Node '.*' initialized).*", 1));
        } else {
            opensearchContainer.waitingFor(Wait.forListeningPorts(EXPOSED_PORT));
        }

        opensearchContainer.start();
    }

    @AfterAll
    static void stopContainers() {
        opensearchContainer.stop();
    }

    @DynamicPropertySource
    public static void opensearchProperties(DynamicPropertyRegistry propertyRegistry) {
        propertyRegistry.add("opensearch.transport.host", opensearchContainer::getHost);
        propertyRegistry.add("opensearch.transport.port", opensearchContainer::getFirstMappedPort);
        propertyRegistry.add("opensearch.skipSslVerification", () -> "true");
        propertyRegistry.add("opensearch.username", () -> "admin");
        propertyRegistry.add("opensearch.password", () -> "admin");
        propertyRegistry.add("opensearch.transport.scheme", () -> "https");
    }

    @Test
    @Order(1)
    @DisplayName("OpenSearch Client should be able to ping OpenSearch instance")
    void shouldReturnTrueWhenOpenSearchIsAvailable() throws IOException {
        assertTrue(openSearchService.ping());
    }

    @Test
    @Order(2)
    @DisplayName("OpenSearch Client should be able to return version of OpenSearch instance")
    void shouldReturnVersionWhenOpenSearchIsAvailable() throws IOException {
        final var versionInfo = openSearchService.getVersion();
        assertEquals(OPEN_SEARCH_VERSION, versionInfo.number());
    }

    @Test
    @Order(3)
    @DisplayName("OpenSearch Client should be able to index a person")
    void shouldIndexPerson() throws IOException {
        final var person = new Person("John", "Doe");
        final var result = openSearchService.indexPerson(person, "persons");
        assertTrue("created".equalsIgnoreCase(result.toString()));
    }

    @Test
    @Order(4)
    @DisplayName("OpenSearch Client should be able to list all persons")
    void shouldListAllPersons() {
        AtomicReference<ArrayList<Person>> persons = new AtomicReference<>(new ArrayList<>());

        await().atMost(5, SECONDS).untilAsserted(() -> {
            persons.set(new ArrayList<>(openSearchService.listAllPersons("persons")));
            assertEquals(1, persons.get().size());
        });

        final var person = persons.get().get(0);
        assertNotNull(person);
        assertEquals("John", person.getFirstName());
        assertEquals("Doe", person.getLastName());
    }
}