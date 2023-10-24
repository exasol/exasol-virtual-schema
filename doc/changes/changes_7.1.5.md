# Exasol Virtual Schema 7.1.5, released 2023-10-24

Code name: Dependency Upgrade

## Summary

This release updates integration tests to use the latest version 8.23.0 of Exasol database Docker Container.

Additionally the release fixes vulnerability CVE-2023-42503 in transitive test dependency to `org.apache.commons:commons-compress` via `exasol-testcontainers` by updating dependencies.

## Refactoring

*#105: Updated tests to use latest version 8.23.0 of Exasol database Docker Container

## Security

*#110: Fixed vulnerability CVE-2023-42503 in test dependency `org.apache.commons:commons-compress`

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:virtual-schema-common-jdbc:11.0.1` to `11.0.2`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.6.1` to `6.6.2`
* Updated `com.exasol:hamcrest-resultset-matcher:1.6.0` to `1.6.1`
* Updated `com.exasol:test-db-builder-java:3.4.2` to `3.5.1`
* Updated `com.exasol:virtual-schema-common-jdbc:11.0.1` to `11.0.2`
* Updated `org.junit.jupiter:junit-jupiter:5.9.3` to `5.10.0`
* Updated `org.mockito:mockito-junit-jupiter:5.4.0` to `5.6.0`
* Updated `org.slf4j:slf4j-jdk14:2.0.7` to `2.0.9`
* Updated `org.testcontainers:junit-jupiter:1.18.3` to `1.19.1`

### Plugin Dependency Updates

* Updated `com.exasol:project-keeper-maven-plugin:2.9.9` to `2.9.12`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.3.0` to `3.4.0`
