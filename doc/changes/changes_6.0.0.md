# Exasol Virtual Schema 6.0.0, released 2021-12-??

Code name: Support native Exasol data types

This release improves support for Exasol's data types `HASHTYPE`, `GEOMETRY`, `INTERVAL DAY TO SECOND`, and `INTERVAL YEAR TO MONTH`. The virtual schema now reports them as their real type, not as `CHAR` or `VARCHAR` as before. **This is a breaking change!**

This release does not support Exasol version 6.2 anymore. Please upgrade to Exasol 7.1 or use version 5.0.5.

## Features

* #60: Support native Exasol data types

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:error-reporting-java:0.4.0` to `0.4.1`
* Updated `com.exasol:exasol-jdbc:7.1.2` to `7.1.3`

### Runtime Dependency Updates

* Removed `org.jacoco:org.jacoco.agent:0.8.7`

### Test Dependency Updates

* Added `org.jacoco:org.jacoco.agent:0.8.7`
* Updated `org.junit.jupiter:junit-jupiter:5.8.1` to `5.8.2`
* Updated `org.mockito:mockito-junit-jupiter:4.0.0` to `4.1.0`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:0.6.0` to `0.7.1`
* Updated `com.exasol:project-keeper-maven-plugin:1.1.0` to `1.3.4`
* Updated `io.github.zlika:reproducible-build-maven-plugin:0.13` to `0.14`
