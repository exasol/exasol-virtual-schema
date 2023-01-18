# Exasol Virtual Schema 7.0.2, released 2023-01-18

Code name: Deployment on Central Repository

## Summary

Exasol's [RLS](https://gihub.com/exasol/row-level-security) implementation depends on the Exasol Virtual Schema. To build it, the JAR file of the VS needs to be on the Central Repository (aka. "Maven Central").

## Features

* #86: Configured deployment on Central Repository

## Dependency Updates

### Plugin Dependency Updates

* Updated `com.exasol:project-keeper-maven-plugin:2.8.0` to `2.9.1`
