# Exasol Virtual Schema 9.0.4, released 2026-09-09

Code name: Fix queries with `NULL` literals

## Summary

This release fixes remote Exasol queries with top-level `NULL` literals.

## Bugfixes

* #124: Fixed remote Exasol queries with top-level `NULL` literals
* #125: Added test to verify that remote Exasol queries with string literals in the select list work

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:exasol-jdbc:26.2.8` to `26.2.9`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:8.0.1` to `8.0.2`
* Updated `org.slf4j:slf4j-jdk14:2.0.18` to `2.0.19`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:2.1.0` to `2.1.1`
* Updated `com.exasol:project-keeper-maven-plugin:5.7.4` to `5.7.5`
* Updated `io.github.git-commit-id:git-commit-id-maven-plugin:10.0.0` to `10.0.1`
* Updated `org.apache.maven.plugins:maven-jar-plugin:3.5.0` to `3.5.1`
* Updated `org.apache.maven.plugins:maven-toolchains-plugin:3.2.0` to `3.3.0`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.7.3` to `1.8.0`
