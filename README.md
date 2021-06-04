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

This project contains the Exasol dialect for the Exasol's Virtual Schema. Use this dialect if you want to create a Virtual Schema based on the Exasol database.

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
* [Dependencies](dependencies.md)

Find all the documentation in the [Virtual Schemas project][vs-doc].

## Information for Developers

* [Virtual Schema API Documentation][vs-api]