# Exasol Virtual Schema 8.0.1, released 2025-06-17

Code name: Timestamp precision

## Summary

This release improves the support for columns types with fractional second precision (FSP), i.e. TIME, DATETIME and
TIMESTAMP. The specified FSP will be maintained in Exasol newer versions (>= 8.32.0)

This release also contains a security update. We updated the dependencies of the project to fix transitive security issues.

We also added an exception for the OSSIndex for CVE-2024-55551, which is a false positive in Exasol's JDBC driver.
This issue has been fixed quite a while back now, but the OSSIndex unfortunately does not contain the fix version of 24.2.1 (2024-12-10) set.

## Features

* #123: TS(9) support in Exasol VS

## Security

* #129:  Fix CVE-2024-55551 in com.exasol:exasol-jdbc:jar:7.1.20:compile

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:exasol-jdbc:7.1.20` to `25.2.3`
* Updated `com.exasol:virtual-schema-common-jdbc:12.0.0` to `13.0.0`
* Updated `org.jacoco:org.jacoco.agent:0.8.11` to `0.8.13`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:7.0.1` to `7.1.6`
* Updated `com.exasol:virtual-schema-common-jdbc:12.0.0` to `13.0.0`
* Updated `org.jacoco:org.jacoco.agent:0.8.11` to `0.8.13`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:2.0.1` to `2.0.3`
* Updated `com.exasol:project-keeper-maven-plugin:4.2.0` to `5.2.2`
* Added `com.exasol:quality-summarizer-maven-plugin:0.2.0`
* Added `io.github.git-commit-id:git-commit-id-maven-plugin:9.0.1`
* Removed `io.github.zlika:reproducible-build-maven-plugin:0.16`
* Added `org.apache.maven.plugins:maven-artifact-plugin:3.6.0`
* Updated `org.apache.maven.plugins:maven-assembly-plugin:3.6.0` to `3.7.1`
* Updated `org.apache.maven.plugins:maven-clean-plugin:3.2.0` to `3.4.1`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.12.1` to `3.14.0`
* Updated `org.apache.maven.plugins:maven-dependency-plugin:3.6.1` to `3.8.1`
* Updated `org.apache.maven.plugins:maven-deploy-plugin:3.1.1` to `3.1.4`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.4.1` to `3.5.0`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.2.5` to `3.5.3`
* Updated `org.apache.maven.plugins:maven-gpg-plugin:3.1.0` to `3.2.7`
* Updated `org.apache.maven.plugins:maven-install-plugin:3.1.2` to `3.1.4`
* Updated `org.apache.maven.plugins:maven-jar-plugin:3.3.0` to `3.4.2`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.6.3` to `3.11.2`
* Updated `org.apache.maven.plugins:maven-site-plugin:3.12.1` to `3.21.0`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.2.5` to `3.5.3`
* Updated `org.apache.maven.plugins:maven-toolchains-plugin:3.1.0` to `3.2.0`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.6.0` to `1.7.0`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.16.2` to `2.18.0`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.11` to `0.8.13`
* Updated `org.sonarsource.scanner.maven:sonar-maven-plugin:3.10.0.2594` to `5.1.0.4751`
* Added `org.sonatype.central:central-publishing-maven-plugin:0.7.0`
* Removed `org.sonatype.plugins:nexus-staging-maven-plugin:1.6.13`
