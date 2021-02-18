# Exasol Virtual Schema Common 5.0.0, released 2020-02-12

Code name: Removed `SQL_DIALECT` property

## Summary

The `SQL_DIALECT` property is obsolete. Please, do not provide this property anymore.

## Feature

* #32: Get Geometry SRID(https://en.wikipedia.org/wiki/Spatial_reference_system#Identifier) from a system table instead of the hardcoded value.

## Refactoring

* #40: Updated to the latest common part and added integration tests for new logic.
* #42: Enabled disabled integration tests after Exasol docker 7.0.7 became available.
* #36: Added error codes to the exception messages.

## Dependency Updates

### Runtime Dependencies

* Added `com.exasol:error-reporting-java:0.2.2`
* Updated `com.exasol:virtual-schema-common-jdbc:7.0.0` to `9.0.1`
* Updated `com.exasol:exasol-jdbc:7.0.3` to `7.0.7`
* Updated `org.jacoco:org.jacoco.agent:0.8.5` to `0.8.6`
  
### Test Dependencies

* Updated `com.exasol:exasol-testcontainer:3.3.1` to `3.5.0`
* Updated `com.exasol:hamcrest-resultset-matcher:1.2.1` to `1.4.0`
* Updated `com.exasol:test-db-builder-java:2.0.0` to `3.0.0`
* Updated `org.mockito:mockito-junit-jupiter:3.6.0` to `3.7.7`
* Updated `org.testcontainers:junit-jupiter:1.15.0` to `1.15.2`
* Updated `org.junit.jupiter:junit-jupiter:5.7.0` to `5.7.1`
* Removed `junit:junit:4.13.1`

### Maven Plugins

* Added `com.exasol:error-code-crawler-maven-plugin:0.1.1`
* Updated `com.exasol:project-keeper-maven-plugin:0.4.0` to `0.4.2`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.7` to `2.8.1`