# OpenSearch Sandbox

## Description

This project is a demonstration and validation of the OpenSearch Java Client's interoperability with different versions
of OpenSearch. It is an over-simplified sample Spring Boot application that uses the OpenSearch Java Client to connect
to an OpenSearch cluster and perform a simple index and search operation.

The different versions of OpenSearch clusters are deployed as Docker containers via Testcontainers. The tests are 
executed against OpenSearch clusters using JUnit 5.

## Requirements

- Java 17
- Maven
- Docker (1.6.0 or higher)

## Installation

```bash
git clone https://github.com/tcsizmadia/opensearch-sandbox.git
cd opensearch-sandbox
mvn clean install
```

## Usage

To perform validations, you need to execute the integration tests with the following command:

```bash
mvn clean verify
```

This will start the default version (1.3.0) of OpenSearch as a single node, execute the tests, and stop the node.

### Testing different versions of OpenSearch

To test different versions of OpenSearch, you need to set the `opensearch.version` property to the desired version:

```bash
mvn clean verify -Dopensearch.version=1.2.0
```

## Contributing

Feel free to add more tests to validate the interoperability of the OpenSearch Java Client with different versions of
OpenSearch and create a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
