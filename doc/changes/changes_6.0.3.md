# Exasol Virtual Schema 6.0.3, released 2022-02-??

Code name: Improved TLS documentation

## Summary

In version 6.0.3 we updated dependent versions and fixed typos in user guide.

The project version in the integration tests with the JAR package is now automatically determined.

This release upgrades dependencies and reduces the number of runtime dependencies, fixing CVE-2022-21724 in the PostgreSQL JDBC driver.

We also fixed the following vulnerabilities by updating other dependencies:

* CVE-2022-24823 (in `io.netty:netty-common`)
* CVE-2016-5003 (in `io.netty:netty-handler`)
* CVE-2016-5002 (in `io.netty:netty-handler`)
* CVE-2016-5004 (in `org.apache.xmlrpc:xmlrpc-client`)

## Bugfixes

* #74: Fixed security issues in dependencies
* #64: Fixed typos in user guide

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:exasol-jdbc:7.1.4` to `7.1.11`
* Updated `com.exasol:virtual-schema-common-jdbc:9.0.4` to `9.0.5`
* Added `org.glassfish:jakarta.json:2.0.1`

### Test Dependency Updates

* Added `com.exasol:exasol-test-setup-abstraction-java:0.3.2`
* Updated `com.exasol:exasol-testcontainers:6.0.0` to `6.1.2`
* Added `com.exasol:maven-project-version-getter:1.1.0`
* Updated `com.exasol:test-db-builder-java:3.2.2` to `3.3.3`
* Updated `com.exasol:udf-debugging-java:0.5.0` to `0.6.4`
* Updated `com.exasol:virtual-schema-common-jdbc:9.0.4` to `9.0.5`
* Updated `org.jacoco:org.jacoco.agent:0.8.7` to `0.8.8`
* Updated `org.mockito:mockito-junit-jupiter:4.3.1` to `4.6.1`
* Updated `org.slf4j:slf4j-jdk14:1.7.35` to `1.7.36`
* Updated `org.testcontainers:junit-jupiter:1.16.3` to `1.17.2`

### Plugin Dependency Updates

* Updated `com.exasol:artifact-reference-checker-maven-plugin:0.4.0` to `0.4.1`
* Updated `com.exasol:error-code-crawler-maven-plugin:0.7.1` to `1.1.1`
* Updated `com.exasol:project-keeper-maven-plugin:1.3.4` to `2.4.6`
* Updated `org.apache.maven.plugins:maven-clean-plugin:3.1.0` to `2.5`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.9.0` to `3.10.1`
* Updated `org.apache.maven.plugins:maven-dependency-plugin:3.2.0` to `3.3.0`
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
