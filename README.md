# Exasol Virtual Schema 

[![Build Status](https://travis-ci.com/exasol/exasol-virtual-schema.svg?branch=master)](https://travis-ci.com/exasol/exasol-virtual-schema)
[![Maven Central](https://img.shields.io/maven-central/v/com.exasol/exasol-virtual-schema)](https://search.maven.org/artifact/com.exasol/exasol-virtual-schema)

SonarCloud results:

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-virtual-schema&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-virtual-schema)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-virtual-schema&metric=security_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-virtual-schema)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-virtual-schema&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-virtual-schema)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-virtual-schema&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-virtual-schema)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-virtual-schema&metric=sqale_index)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-virtual-schema)

[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-virtual-schema&metric=code_smells)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-virtual-schema)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-virtual-schema&metric=coverage)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-virtual-schema)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-virtual-schema&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-virtual-schema)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-virtual-schema&metric=ncloc)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-virtual-schema)

This project contains the Exasol dialect for the Exasol's Virtual Schema.
Use this dialect if you want to create a Virtual Schema based on the Exasol database.

If you want to set up a Virtual Schema for a different database system, please head over to the [Virtual Schemas Repository][virtual-schemas].

## Features

* Access Exasol database using Virtual Schema.

## Deprecation Warning

Starting with version 4.0.0 of the Exasol SQL dialect, the new `EXA_CONNECTION` property replaces the `EXA_CONNECTION_STRING` property, for improved security.

It takes the name of a connection definition in case you are using `IMPORT FROM EXA`. For details please consult the [user guide][user-guide].

## Table of Contents

### Information for Users

* [Exasol dialect](doc/dialects/exasol.md)
* [User Guide][user-guide]
* [Changelog](doc/changes/changelog.md)

Find all the documentation in the [Virtual Schemas project][vs-doc].

## Information for Developers 

* [Virtual Schema API Documentation][vs-api]

### Run Time Dependencies

Running the Virtual Schema requires a Java Runtime version 11 or later.

| Dependency                                                         | Purpose                                                | License                       |
|--------------------------------------------------------------------|--------------------------------------------------------|-------------------------------|
| [Exasol Virtual Schema JDBC][virtual-schema-common-jdbc]           | Common JDBC functions for Virtual Schemas adapters     | MIT License                   |
| [Exasol JDBC Driver][exasol-jdbc-driver]                           | JDBC driver for Exasol database              | MIT License                   |

### Test Dependencies

| Dependency                                                         | Purpose                                                | License                       |
|--------------------------------------------------------------------|--------------------------------------------------------|-------------------------------|
| [Java Hamcrest](http://hamcrest.org/JavaHamcrest/)                 | Checking for conditions in code via matchers           | BSD License                   |
| [JUnit](https://junit.org/junit5)                                  | Unit testing framework                                 | Eclipse Public License 1.0    |
| [Mockito](http://site.mockito.org/)                                | Mocking framework                                      | MIT License                   |
| [Testcontainers](https://www.testcontainers.org/)                  | Container-based integration tests                      | MIT License                   |
| [SLF4J](http://www.slf4j.org/)                                     | Logging facade                                         | MIT License                   |


### Maven Plug-ins

| Plug-in                                                            | Purpose                                                | License                       |
|--------------------------------------------------------------------|--------------------------------------------------------|-------------------------------|
| [Artifact Reference Checker Plugin][artifact-ref-checker-plugin]   | Check if artifact is referenced with correct version   | MIT License                   |
| [Maven Compiler Plugin][maven-compiler-plugin]                     | Setting required Java version                          | Apache License 2.0            |
| [Maven Exec Plugin](https://www.mojohaus.org/exec-maven-plugin/)   | Executing external applications                        | Apache License 2.0            |
| [Maven Enforcer Plugin][maven-enforcer-plugin]                     | Controlling environment constants                      | Apache License 2.0            |
| [Maven GPG Plugin][maven-gpg-plugin]                               | Code signing                                           | Apache License 2.0            |
| [Maven Failsafe Plugin][maven-failsafe-plugin]                     | Integration testing                                    | Apache License 2.0            |
| [Maven Javadoc Plugin][maven-javadoc-plugin]                       | Creating a Javadoc JAR                                 | Apache License 2.0            |
| [Maven Jacoco Plugin][maven-jacoco-plugin]                         | Code coverage metering                                 | Eclipse Public License 2.0    |
| [Maven Source Plugin][maven-source-plugin]                         | Creating a source code JAR                             | Apache License 2.0            |
| [Maven Surefire Plugin][maven-surefire-plugin]                     | Unit testing                                           | Apache License 2.0            |
| [Sonatype OSS Index Maven Plugin][sonatype-oss-index-maven-plugin] | Checking Dependencies Vulnerability                    | ASL2                          |
| [Versions Maven Plugin][versions-maven-plugin]                     | Checking if dependencies updates are available         | Apache License 2.0            |
| [Test Database Builder][test-db-builder]                           | Fluent database interfaces for testing                 | MIT License                   |

[artifact-ref-checker-plugin]: https://github.com/exasol/artifact-reference-checker-maven-plugin
[exasol-jdbc-driver]: https://www.exasol.com/portal/display/DOWNLOAD/Exasol+Download+Section
[maven-compiler-plugin]: https://maven.apache.org/plugins/maven-compiler-plugin/
[maven-enforcer-plugin]: http://maven.apache.org/enforcer/maven-enforcer-plugin/
[maven-gpg-plugin]: https://maven.apache.org/plugins/maven-gpg-plugin/
[maven-failsafe-plugin]: https://maven.apache.org/surefire/maven-failsafe-plugin/
[maven-javadoc-plugin]: https://maven.apache.org/plugins/maven-javadoc-plugin/
[maven-jacoco-plugin]: https://www.eclemma.org/jacoco/trunk/doc/maven.html
[maven-source-plugin]: https://maven.apache.org/plugins/maven-source-plugin/
[maven-surefire-plugin]: https://maven.apache.org/surefire/maven-surefire-plugin/
[sonatype-oss-index-maven-plugin]: https://sonatype.github.io/ossindex-maven/maven-plugin/
[test-db-builder]: https://github.com/exasol/test-db-builder/
[versions-maven-plugin]: https://www.mojohaus.org/versions-maven-plugin/
[virtual-schema-common-jdbc]: https://github.com/exasol/virtual-schema-common-jdbc

[user-guide]: https://docs.exasol.com/database_concepts/virtual_schemas.htm
[virtual-schemas]: https://github.com/exasol/virtual-schemas
[vs-api]: https://github.com/exasol/virtual-schema-common-java/blob/master/doc/development/api/virtual_schema_api.md
[vs-doc]: https://github.com/exasol/virtual-schemas/tree/master/doc
