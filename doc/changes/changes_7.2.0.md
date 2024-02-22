# Exasol Virtual Schema 7.2.0, released 2024-02-22

Code name: Fix data types for `IMPORT FROM EXA`

## Summary

This release fixes the data types reported for virtual schemas using `IMPORT FROM EXA`. The Exasol specific types `GEOMETRY`, `INTERVAL YEAR TO MONTH`, `INTERVAL DAY TO SECOND` and `HASHTYPE` were mapped to `VARCHAR` before. The virtual schema now returns these types correctly.

This also fixes a bug when joining a table in a virtual schema with a normal table using a `HASHTYPE` column. This failed before in Exasol 7.1 with error message `Feature not supported: Incomparable Types: VARCHAR(32) UTF8 and HASHTYPE(16 BYTE)!`. In Exasol 8 the error message was `Adapter generated invalid pushdown query for virtual table VIRTUAL: Data type mismatch in column number 1 (1-indexed).Expected HASHTYPE(16 BYTE), but got VARCHAR(32) UTF8.`.


This release also fixes vulnerabilities CVE-2024-25710 and CVE-2024-26308 in transitive test dependency `org.apache.commons:commons-compress`.

## Security

* #120: Fixed CVE-2024-25710 in `org.apache.commons:commons-compress`
* #121: Fixed CVE-2024-26308 in `org.apache.commons:commons-compress`

## Bugfixes

* #119: Fixed data types for `IMPORT FROM EXA`

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:virtual-schema-common-jdbc:11.0.2` to `12.0.0`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.6.3` to `7.0.1`
* Updated `com.exasol:hamcrest-resultset-matcher:1.6.3` to `1.6.4`
* Updated `com.exasol:test-db-builder-java:3.5.2` to `3.5.3`
* Updated `com.exasol:virtual-schema-common-jdbc:11.0.2` to `12.0.0`
* Updated `org.junit.jupiter:junit-jupiter:5.10.1` to `5.10.2`
* Updated `org.mockito:mockito-junit-jupiter:5.7.0` to `5.10.0`
* Updated `org.slf4j:slf4j-jdk14:2.0.9` to `2.0.12`
* Updated `org.testcontainers:junit-jupiter:1.19.2` to `1.19.6`

### Plugin Dependency Updates

* Updated `com.exasol:project-keeper-maven-plugin:2.9.16` to `3.0.1`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.2.2` to `3.2.3`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.2.2` to `3.2.3`
* Added `org.apache.maven.plugins:maven-toolchains-plugin:3.1.0`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.16.1` to `2.16.2`
