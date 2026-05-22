# Exasol Virtual Schema 9.0.0, released 2026-??-??

Code name: Anonymous telemetry

## Summary

This release introduces anonymous feature-usage telemetry through `telemetry-java`. See the [documentation](https://github.com/exasol/telemetry-java/blob/main/doc/app-user-guide.md) for details about the collected data and how to opt out.

## Breaking Change

Starting with this release, this Virtual Schema no longer supports Exasol 7.1. The supported versions are the current release and the LTS release line `2025.1.x`.

## Features

* #139: Added anonymous feature-usage tracking

## Bugfixes

* #138: Fixed the reported adapter version

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:error-reporting-java:1.0.1` to `1.0.2`
* Updated `com.exasol:exasol-jdbc:25.2.3` to `26.2.7`
* Updated `com.exasol:virtual-schema-common-jdbc:13.0.1` to `14.0.2`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:7.1.7` to `7.3.0`
* Updated `com.exasol:hamcrest-resultset-matcher:1.7.1` to `1.7.2`
* Updated `com.exasol:maven-project-version-getter:1.2.1` to `1.2.2`
* Updated `com.exasol:test-db-builder-java:3.6.2` to `4.0.0`
* Updated `com.exasol:udf-debugging-java:0.6.16` to `0.6.18`
* Updated `com.exasol:virtual-schema-common-jdbc:13.0.1` to `14.0.2`
* Updated `org.junit.jupiter:junit-jupiter-api:5.13.1` to `5.14.4`
* Updated `org.junit.jupiter:junit-jupiter-params:5.13.1` to `5.14.4`
* Updated `org.mockito:mockito-junit-jupiter:5.18.0` to `5.23.0`
* Updated `org.slf4j:slf4j-jdk14:2.0.17` to `2.0.18`
* Updated `org.testcontainers:junit-jupiter:1.21.1` to `1.21.4`

### Plugin Dependency Updates

* Updated `com.exasol:artifact-reference-checker-maven-plugin:0.4.1` to `0.4.4`
* Updated `com.exasol:error-code-crawler-maven-plugin:2.0.5` to `2.0.7`
* Updated `com.exasol:project-keeper-maven-plugin:5.4.3` to `5.6.2`
* Updated `io.github.git-commit-id:git-commit-id-maven-plugin:9.0.2` to `10.0.0`
* Updated `org.apache.maven.plugins:maven-assembly-plugin:3.7.1` to `3.8.0`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.14.1` to `3.15.0`
* Updated `org.apache.maven.plugins:maven-dependency-plugin:3.9.0` to `3.10.0`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.5.4` to `3.5.5`
* Updated `org.apache.maven.plugins:maven-jar-plugin:3.4.2` to `3.5.0`
* Updated `org.apache.maven.plugins:maven-resources-plugin:3.3.1` to `3.5.0`
* Updated `org.apache.maven.plugins:maven-source-plugin:3.2.1` to `3.4.0`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.5.4` to `3.5.5`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.19.1` to `2.21.0`
* Updated `org.sonarsource.scanner.maven:sonar-maven-plugin:5.2.0.4988` to `5.5.0.6356`
* Updated `org.sonatype.central:central-publishing-maven-plugin:0.9.0` to `0.10.0`
