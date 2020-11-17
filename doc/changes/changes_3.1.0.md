# Exasol Virtual Schema Common 3.1.0, released 2020-11-17

Code name: Added new functions support, detect interval precision

## Bugs

* #30: Fixed a CVE-2020-15250 in a transitive dependency.

## Features / Enhancements

* #16: Get interval precision from system table.

## Documentation

* #15: Improved the documentation.

## Refactoring:

* #18: Use test-db-builder for integration tests.
* #22: Updated to the new major version of `virtual-schema-common-jdbc`.
* #26: Updated to the new major version of `virtual-schema-common-jdbc`.

## Dependency updates

* Added com.exasol:test-db-builder-java:1.1.0
* Added com.exasol:artifact-reference-checker-maven-plugin:0.3.1
* Added org.junit.jupiter:junit-jupiter:5.7.0
* Updated com.exasol:exasol-testcontainers:jar:2.0.3 to version 3.3.1
* Updated com.exasol:exasol-jdbc:6.2.5 to version 7.0.3
* Updated com.exasol:virtual-schema-common-jdbc:5.0.4 to version 7.0.0
* Updated com.exasol:hamcrest-resultset-matcher:1.1.1 to version 1.2.1
* Updated org.mockito:mockito-junit-jupiter:3.3.3 to version 3.6.0
* Updated org.testcontainers:junit-jupiter:1.14.3 to version 1.15.0
* Removed org.junit.jupiter:junit-jupiter-engine
* Removed org.junit.jupiter:junit-jupiter-params
* Removed org.junit.platform:junit-platform-runner