# Exasol Virtual Schema 6.0.0, released 2022-01-18

Code name: Support native Exasol data types

## Summary

This release improves support for Exasol's data types `HASHTYPE`, `GEOMETRY`, `INTERVAL DAY TO SECOND`, and `INTERVAL YEAR TO MONTH`. The virtual schema now reports them as their real type, not as `CHAR` or `VARCHAR` as before. **This is a breaking change!**

This release does not support Exasol version 6.2 any more. Please upgrade to Exasol 7.1 or use Exasol Virtual Schema version 5.0.5.

## Features

* #60: Support native Exasol data types

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:error-reporting-java:0.4.0` to `0.4.1`
* Updated `com.exasol:exasol-jdbc:7.1.2` to `7.1.4`

### Runtime Dependency Updates

* Removed `org.jacoco:org.jacoco.agent:0.8.7`

### Test Dependency Updates

* Updated `com.exasol:test-db-builder-java:3.2.1` to `3.2.2`
* Updated `com.exasol:udf-debugging-java:0.4.1` to `0.5.0`
* Added `org.jacoco:org.jacoco.agent:0.8.7`
* Updated `org.junit.jupiter:junit-jupiter:5.8.1` to `5.8.2`
* Updated `org.mockito:mockito-junit-jupiter:4.0.0` to `4.2.0`
* Updated `org.slf4j:slf4j-jdk14:1.7.32` to `1.7.33`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:0.6.0` to `0.7.1`
* Updated `com.exasol:project-keeper-maven-plugin:1.1.0` to `1.3.4`
* Updated `io.github.zlika:reproducible-build-maven-plugin:0.13` to `0.15`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.8.1` to `3.9.0`
* Updated `org.apache.maven.plugins:maven-jar-plugin:3.2.0` to `3.2.2`
* Updated `org.apache.maven.plugins:maven-site-plugin:3.9.1` to `3.10.0`
