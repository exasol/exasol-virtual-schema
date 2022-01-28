# Exasol Virtual Schema 5.0.6, released 2022-01-??

Code name: Revert changes from 6.0.0

## Summary

[Issue #66](https://github.com/exasol/exasol-virtual-schema/issues/66) showed issues with [PR #62](https://github.com/exasol/exasol-virtual-schema/pull/62) that can't easily be addressed. That's why we reverted the code changes from 6.0.0 back to 5.0.5 in preparation for fixing [issue #60](https://github.com/exasol/exasol-virtual-schema/issues/60) in a separate change.

## Bugfixes

* #67: Reverted changes from 6.0.0

## Features

## Dependency Updates

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:5.1.1` to `6.0.0`
* Updated `org.mockito:mockito-junit-jupiter:4.2.0` to `4.3.1`
* Updated `org.slf4j:slf4j-jdk14:1.7.33` to `1.7.35`
* Updated `org.testcontainers:junit-jupiter:1.16.2` to `1.16.3`

### Plugin Dependency Updates

* Updated `org.codehaus.mojo:versions-maven-plugin:2.8.1` to `2.9.0`
