# Exasol Virtual Schema 7.1.6, released 2023-11-21

Code name: Fix CVE-2023-4043 in test dependency `org.eclipse.parsson:parsson`

## Summary

This release fixes vulnerability CVE-2023-4043 in test dependency `org.eclipse.parsson:parsson`. The release also adds runs integration tests using Exasol DB version 8.

## Security

* #114: Fixed CVE-2023-4043 in test dependency `org.eclipse.parsson:parsson`

## Dependency Updates

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.6.2` to `6.6.3`
* Updated `com.exasol:hamcrest-resultset-matcher:1.6.1` to `1.6.3`
* Updated `com.exasol:test-db-builder-java:3.5.1` to `3.5.2`
* Updated `org.jacoco:org.jacoco.agent:0.8.10` to `0.8.11`
* Updated `org.junit.jupiter:junit-jupiter:5.10.0` to `5.10.1`
* Updated `org.mockito:mockito-junit-jupiter:5.6.0` to `5.7.0`
* Updated `org.testcontainers:junit-jupiter:1.19.1` to `1.19.2`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.3.0` to `1.3.1`
* Updated `com.exasol:project-keeper-maven-plugin:2.9.12` to `2.9.16`
* Updated `org.apache.maven.plugins:maven-dependency-plugin:3.6.0` to `3.6.1`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.4.0` to `3.4.1`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.1.2` to `3.2.2`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.1.2` to `3.2.2`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.16.0` to `2.16.1`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.10` to `0.8.11`
* Updated `org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184` to `3.10.0.2594`
