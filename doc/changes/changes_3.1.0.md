# Exasol Virtual Schema Common 3.1.0, released 2020-09-30

Code name: Added new functions support, detect interval precision

## Features / Enhancements

* #16: Get interval precision from system table.

## Documentation

* #15: Improved the documentation.

## Refactoring:

* #18: Use test-db-builder for integration tests.
* #22: Updated to the new major version of `virtual-schema-common-jdbc`.

## Dependency updates

* Added com.exasol:test-db-builder-java:jar:1.1.0
* Added com.exasol:artifact-reference-checker-maven-plugin:jar:0.3.1
* Added org.junit.jupiter:junit-jupiter:jar:5.7.0
* Updated com.exasol:exasol-testcontainers:jar:2.0.3 to version 3.0.0
* Updated com.exasol:exasol-jdbc:jar:6.2.5 to version 7.0.0
* Updated com.exasol:hamcrest-resultset-matcher:jar:1.1.1 to version 1.2.0
* Updated org.mockito:mockito-junit-jupiter:jar:3.3.3 to version 3.5.13
* Removed org.junit.jupiter:junit-jupiter-engine
* Removed org.junit.jupiter:junit-jupiter-params
* Removed org.junit.platform:junit-platform-runner