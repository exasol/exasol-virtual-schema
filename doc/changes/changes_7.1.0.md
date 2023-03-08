# Exasol Virtual Schema 7.1.0, released 2023-??-??

Code name: Mandatory Property SCHEMA_NAME

## Summary

This release updates VSEXA to take virtual schema property `SCHEMA_NAME` as mandatory in order to fix a bug that has been reported when SCHEMA_NAME property is not set.

## Bugfixes

* #92: Broken handling of tables when SCHEMA_NAME property is not set

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:error-reporting-java:1.0.0` to `1.0.1`
* Updated `com.exasol:virtual-schema-common-jdbc:10.1.0` to `10.2.0`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.5.0` to `6.5.1`
* Updated `com.exasol:test-db-builder-java:3.4.1` to `3.4.2`
* Updated `com.exasol:virtual-schema-common-jdbc:10.1.0` to `10.2.0`
* Updated `org.mockito:mockito-junit-jupiter:5.0.0` to `5.1.1`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.2.1` to `1.2.2`
* Updated `com.exasol:project-keeper-maven-plugin:2.9.1` to `2.9.3`
* Updated `org.apache.maven.plugins:maven-dependency-plugin:3.3.0` to `3.5.0`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.0.0-M7` to `3.0.0-M8`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M7` to `3.0.0-M8`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.13.0` to `2.14.2`
