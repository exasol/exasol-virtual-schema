# Exasol Virtual Schema 7.1.1, released 2023-03-29

Code name: Minor Fixes on Top of Version 7.1.0

## Summary

This release fixes some minor problems and adds additional integration tests incl. a test for escaping wildcard when retrieving table metadata from JDBC driver and removes local classes in favor of those provided by version 10.4.0 of VSCJDBC.

## Bugfixes

* #89: Fixed inconsistent semantics of adapter property `IMPORT_FROM_EXA`
* #90: Added test to verify that `ALTER VIRTUAL SCHEMA` triggers adapter properties validation

## Documentation

* #94: Added test for Add test for escaping wildcard when retrieving table metadata from JDBC driver.
* #95: Removed duplicated classes

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:exasol-jdbc:7.1.17` to `7.1.19`
* Updated `com.exasol:virtual-schema-common-jdbc:10.2.0` to `10.5.0`

### Test Dependency Updates

* Updated `com.exasol:hamcrest-resultset-matcher:1.5.2` to `1.5.3`
* Updated `com.exasol:virtual-schema-common-jdbc:10.2.0` to `10.5.0`
* Updated `org.mockito:mockito-junit-jupiter:5.1.1` to `5.2.0`
* Updated `org.slf4j:slf4j-jdk14:2.0.6` to `2.0.7`

### Plugin Dependency Updates

* Updated `com.exasol:project-keeper-maven-plugin:2.9.3` to `2.9.6`
* Updated `org.apache.maven.plugins:maven-deploy-plugin:3.0.0` to `3.1.0`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.1.0` to `3.2.1`
