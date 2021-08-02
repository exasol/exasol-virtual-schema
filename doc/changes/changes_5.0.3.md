# Exasol Virtual Schema 5.0.3, released 2021-08-03

Code name: Dependency Updates

## Summary

In this release we updated the dependencies. By that we fixed transitive CVE-2021-36090.

Bug Fixes:

* #52: Fixed broken links

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:exasol-jdbc:7.0.7` to `7.0.11`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:3.5.1` to `4.0.0`
* Updated `com.exasol:hamcrest-resultset-matcher:1.4.0` to `1.4.1`
* Updated `com.exasol:test-db-builder-java:3.1.1` to `3.2.1`
* Updated `com.exasol:udf-debugging-java:0.3.0` to `0.4.0`
* Updated `org.mockito:mockito-junit-jupiter:3.11.0` to `3.11.2`
* Updated `org.slf4j:slf4j-jdk14:1.7.30` to `1.7.32`
* Updated `org.testcontainers:junit-jupiter:1.15.3` to `1.16.0`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:0.4.0` to `0.5.1`
* Added `com.exasol:project-keeper-maven-plugin:0.10.0`
