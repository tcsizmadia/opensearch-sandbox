package com.github.tcsizmadia.opensearchsandbox.components;

import com.github.tcsizmadia.opensearchsandbox.entities.Person;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchVersionInfo;
import org.opensearch.client.opensearch._types.Result;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class OpenSearchService {

    private final OpenSearchClient openSearchClient;

    public OpenSearchService(OpenSearchClient openSearchClient) {
        this.openSearchClient = openSearchClient;
    }

    public boolean ping() throws IOException {
        return openSearchClient.ping().value();
    }

    public OpenSearchVersionInfo getVersion() throws IOException {
        return openSearchClient.info().version();
    }

    public Result indexPerson(Person document, String indexName) throws IOException {
        final var request = new IndexRequest.Builder<Person>().index(indexName).document(document).build();
        final var response = openSearchClient.index(request);
        return response.result();
    }

    public List<Person> listAllPersons(String indexName) throws IOException {
        SearchResponse<Person> response = openSearchClient.search(s -> s.index(indexName), Person.class);
        List<Person> persons = new ArrayList<>();
        for (int i = 0; i < response.hits().hits().size(); i++) {
            persons.add(response.hits().hits().get(i).source());
        }

        return persons;
    }
}