# Exasol Virtual Schema 7.0.0, released 2022-09-14

Code name: Read Expected Datatypes From Request

## Summary

Starting with major version 8 Exasol database uses the capabilities reported by each virtual schema to provide select list data types for each push down request. Based on this information the JDBC virtual schemas no longer need to infer the data types of the result set by inspecting its values. Instead the JDBC virtual schemas can now use the information provided by the database.

## Bug Fixes

* #71: Correctly map INTERVAL and GEOMETRY types

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:exasol-jdbc:7.1.4` to `7.1.11`
* Updated `com.exasol:virtual-schema-common-jdbc:9.0.4` to `10.0.0`
* Added `org.glassfish:jakarta.json:2.0.1`

### Test Dependency Updates

* Added `com.exasol:exasol-test-setup-abstraction-java:0.3.2`
* Updated `com.exasol:exasol-testcontainers:6.0.0` to `6.2.0`
* Added `com.exasol:maven-project-version-getter:1.1.0`
* Updated `com.exasol:test-db-builder-java:3.2.2` to `3.3.3`
* Updated `com.exasol:udf-debugging-java:0.5.0` to `0.6.4`
* Updated `com.exasol:virtual-schema-common-jdbc:9.0.4` to `10.0.0`
* Updated `org.jacoco:org.jacoco.agent:0.8.7` to `0.8.8`
* Updated `org.mockito:mockito-junit-jupiter:4.3.1` to `4.6.1`
* Updated `org.slf4j:slf4j-jdk14:1.7.35` to `1.7.36`
* Updated `org.testcontainers:junit-jupiter:1.16.3` to `1.17.2`

### Plugin Dependency Updates

* Updated `com.exasol:artifact-reference-checker-maven-plugin:0.4.0` to `0.4.1`
* Updated `com.exasol:error-code-crawler-maven-plugin:0.7.1` to `1.1.2`
* Updated `com.exasol:project-keeper-maven-plugin:1.3.4` to `2.7.0`
* Updated `org.apache.maven.plugins:maven-clean-plugin:3.1.0` to `2.5`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.9.0` to `3.10.1`
* Updated `org.apache.maven.plugins:maven-dependency-plugin:3.2.0` to `3.3.0`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.0.0` to `3.1.0`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.0.0-M3` to `3.0.0-M5`
* Updated `org.apache.maven.plugins:maven-install-plugin:3.0.0-M1` to `2.4`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.3.1` to `3.4.0`
* Updated `org.apache.maven.plugins:maven-resources-plugin:3.2.0` to `2.6`
* Updated `org.apache.maven.plugins:maven-site-plugin:3.10.0` to `3.3`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M3` to `3.0.0-M5`
* Added `org.codehaus.mojo:flatten-maven-plugin:1.2.7`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.9.0` to `2.10.0`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.7` to `0.8.8`
* Added `org.sonarsource.scanner.maven:sonar-maven-plugin:3.9.1.2184`
* Updated `org.sonatype.plugins:nexus-staging-maven-plugin:1.6.8` to `1.6.13`
