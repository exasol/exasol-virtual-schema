# Exasol Virtual Schema 7.2.0, released 2024-02-22

Code name: Add parameter `GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA`

## Summary

Using `IMPORT FROM EXA` might lead to some unexpected datatype mappings. Unlike for a JDBC connection there's no explicit data mapping being generated when using `IMPORT FROM EXA`. The Exasol specific types `GEOMETRY`, `INTERVAL YEAR TO MONTH`, `INTERVAL DAY TO SECOND` and `HASHTYPE` are mapped to `VARCHAR`. This release adds parameter `GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA`. When setting this to `true`, the data types are mapped as expected.

Setting `GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA` to `true` also fixes a bug when joining a table in a virtual schema with a normal table using a `HASHTYPE` column. This failed before in Exasol 7.1 with error message `Feature not supported: Incomparable Types: VARCHAR(32) UTF8 and HASHTYPE(16 BYTE)!`.

See the [user guide](../dialects/exasol.md#auto-generated-datatype-mapping-using-exa-import) for details.

This release also fixes vulnerabilities CVE-2024-25710 and CVE-2024-26308 in transitive test dependency `org.apache.commons:commons-compress`.

## Security

* #120: Fixed CVE-2024-25710 in `org.apache.commons:commons-compress`
* #121: Fixed CVE-2024-26308 in `org.apache.commons:commons-compress`

## Bugfixes

* #119: Fixed data types for `IMPORT FROM EXA`

## Dependency Updates

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.6.3` to `7.0.1`
* Updated `com.exasol:hamcrest-resultset-matcher:1.6.3` to `1.6.4`
* Updated `com.exasol:test-db-builder-java:3.5.2` to `3.5.3`
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
