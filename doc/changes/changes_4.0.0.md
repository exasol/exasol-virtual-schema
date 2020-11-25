# Exasol Virtual Schema Common 4.0.0, released 2020-11-25

Code name: Fixed credentials exposure in EXA connection

In this release we fixed a credential exposure that happened when you used `IMPORT ... FROM EXA`.

## Breaking Changes

If you used `IMPORT ... FROM EXA` in a previous version, you need to remove the old property `EXA_CONNECTION_STRING`,
create a named connection definition of type EXA with `CREATE CONNECTION` and provide the name of that definition in the
new property `EXA_CONNECTION`.

The old variant is intentionally not supported anymore to tighten security.

## Bug Fixes

* #24: Get interval precision from system table.

## Refactoring

* #24: Refactored integration tests.

## Test Dependency Updates

* Added `com.exasol:udf-debugging-java:0.3.0`
* Added `org.jacoco:org.jacoco.agent:0.8.5`

## Plugin Updates

* Added `com.exasol:project-keeper-maven-plugin:0.4.0`
* Added `org.apache.maven.plugins:maven-dependency-plugin:3.1.2`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.5` to `0.8.6`