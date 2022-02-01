# Exasol Virtual Schema 6.0.1, released 2022-01-31

Code name: Fix data type mapping

## Summary

In this release we reverted the changed mapping from [PR #62](https://github.com/exasol/exasol-virtual-schema/pull/62) for data types `INTERVAL` and `GEOMETRY` because the mapping of types in the generated `IMPORT` query was wrong. These data types are now mapped to `VARCHAR` as before. `HASHTYPE` columns however are now mapped correctly with their correct length.

## Bugfixes

* #67: Reverted changes from 6.0.0
* #69: Fixed mapping of `HASHTYPE` columns

## Features

## Dependency Updates

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:5.1.1` to `6.0.0`
* Updated `org.mockito:mockito-junit-jupiter:4.2.0` to `4.3.1`
* Updated `org.slf4j:slf4j-jdk14:1.7.33` to `1.7.35`
* Updated `org.testcontainers:junit-jupiter:1.16.2` to `1.16.3`

### Plugin Dependency Updates

* Updated `org.codehaus.mojo:versions-maven-plugin:2.8.1` to `2.9.0`
