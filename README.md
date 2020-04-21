# Exasol Virtual Schema 

[![Build Status](https://travis-ci.com/exasol/exasol-virtual-schema.svg?branch=master)](https://travis-ci.com/exasol/exasol-virtual-schema)

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

This projects contains the Exasol dialect for [Exasol's Virtual Schema][virtual-schemas].

## Features

* Access Exasol database using Virtual Schema.

## Table of Contents

### Information for Users

* [Exasol dialect](doc/dialects/exasol.md)
* [User guide][user-guide]

Find all the documentation in the [Virtual Schemas project][vs-doc].

### Run Time Dependencies

Running the Virtual Schema requires a Java Runtime version 11 or later.

| Dependency                                                                          | Purpose                                                | License                       |
|-------------------------------------------------------------------------------------|--------------------------------------------------------|-------------------------------|
| [Exasol Virtual Schema Common](https://github.com/exasol/virtual-schema-common-java)| Common module of Exasol Virtual Schemas adapters       | MIT License                   |
| [Exasol Virtual Schema JDBC](https://github.com/exasol/virtual-schema-common-jdbc)  | Common JDBC functions for Virtual Schemas adapters     | MIT License                   |
| [Exasol JDBC Driver](https://www.exasol.com/portal/display/DOWNLOAD/Exasol+Download+Section)  | JDBC driver for Exasol database              | MIT License                   |

### Test Dependencies

| Dependency                                                                          | Purpose                                                | License                       |
|-------------------------------------------------------------------------------------|--------------------------------------------------------|-------------------------------|
| [Java Hamcrest](http://hamcrest.org/JavaHamcrest/)                                  | Checking for conditions in code via matchers           | BSD License                   |
| [JUnit](https://junit.org/junit5)                                                   | Unit testing framework                                 | Eclipse Public License 1.0    |
| [Mockito](http://site.mockito.org/)                                                 | Mocking framework                                      | MIT License                   |
| [Testcontainers](https://www.testcontainers.org/)                                   | Container-based integration tests                      | MIT License                   |
| [SLF4J](http://www.slf4j.org/)                                                      | Logging facade                                         | MIT License                   |


### Maven Plug-ins

| Plug-in                                                                             | Purpose                                                | License                       |
|-------------------------------------------------------------------------------------|--------------------------------------------------------|-------------------------------|
| [Maven Compiler Plugin](https://maven.apache.org/plugins/maven-compiler-plugin/)    | Setting required Java version                          | Apache License 2.0            |
| [Maven Exec Plugin](https://www.mojohaus.org/exec-maven-plugin/)                    | Executing external applications                        | Apache License 2.0            |
| [Maven GPG Plugin](https://maven.apache.org/plugins/maven-gpg-plugin/)              | Code signing                                           | Apache License 2.0            |
| [Maven Failsafe Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)   | Integration testing                                    | Apache License 2.0            |
| [Maven Javadoc Plugin](https://maven.apache.org/plugins/maven-javadoc-plugin/)      | Creating a Javadoc JAR                                 | Apache License 2.0            |
| [Maven Jacoco Plugin](https://www.eclemma.org/jacoco/trunk/doc/maven.html)          | Code coverage metering                                 | Eclipse Public License 2.0    |
| [Maven Source Plugin](https://maven.apache.org/plugins/maven-source-plugin/)        | Creating a source code JAR                             | Apache License 2.0            |
| [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)   | Unit testing                                           | Apache License 2.0            |

[user-guide]: https://github.com/exasol/virtual-schemas/blob/master/doc/user-guide/user_guide.md#using-the-adapter
[virtual-schemas]: https://github.com/exasol/virtual-schemas
[vs-doc]: https://github.com/exasol/virtual-schemas/tree/master/doc