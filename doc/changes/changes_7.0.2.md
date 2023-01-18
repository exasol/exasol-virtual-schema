# Exasol Virtual Schema 7.0.2, released 2023-01-18

Code name: Deployment on Central Repository

## Summary

Exasol's [RLS](https://gihub.com/exasol/row-level-security) implementation depends on the Exasol Virtual Schema. To build it, the JAR file of the VS needs to be on the Central Repository (aka. "Maven Central").

We removed some debugging dependencies to reduce the complexity of the project.

## Refactoring

* #86: Configured deployment on Central Repository

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:error-reporting-java:0.4.1` to `1.0.0`
* Updated `com.exasol:exasol-jdbc:7.1.11` to `7.1.17`
* Updated `com.exasol:virtual-schema-common-jdbc:10.0.1` to `10.1.0`

### Test Dependency Updates

* Removed `com.exasol:exasol-test-setup-abstraction-java:0.3.2`
* Updated `com.exasol:exasol-testcontainers:6.2.0` to `6.5.0`
* Updated `com.exasol:hamcrest-resultset-matcher:1.5.1` to `1.5.2`
* Updated `com.exasol:maven-project-version-getter:1.1.0` to `1.2.0`
* Updated `com.exasol:test-db-builder-java:3.3.3` to `3.4.1`
* Removed `com.exasol:udf-debugging-java:0.6.4`
* Updated `com.exasol:virtual-schema-common-jdbc:10.0.1` to `10.1.0`
* Updated `org.junit.jupiter:junit-jupiter:5.8.2` to `5.9.2`
* Updated `org.mockito:mockito-junit-jupiter:4.6.1` to `5.0.0`
* Updated `org.slf4j:slf4j-jdk14:1.7.36` to `2.0.6`
* Updated `org.testcontainers:junit-jupiter:1.17.2` to `1.17.6`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.1.2` to `1.2.1`
* Updated `com.exasol:project-keeper-maven-plugin:2.8.0` to `2.9.1`
* Updated `io.github.zlika:reproducible-build-maven-plugin:0.15` to `0.16`
* Updated `org.apache.maven.plugins:maven-deploy-plugin:2.7` to `3.0.0`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.0.0-M5` to `3.0.0-M7`
* Added `org.apache.maven.plugins:maven-gpg-plugin:3.0.1`
* Updated `org.apache.maven.plugins:maven-jar-plugin:3.2.2` to `3.3.0`
* Added `org.apache.maven.plugins:maven-javadoc-plugin:3.4.1`
* Added `org.apache.maven.plugins:maven-source-plugin:3.2.1`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M5` to `3.0.0-M7`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.2.7` to `1.3.0`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.10.0` to `2.13.0`
* Added `org.sonatype.plugins:nexus-staging-maven-plugin:1.6.13`
