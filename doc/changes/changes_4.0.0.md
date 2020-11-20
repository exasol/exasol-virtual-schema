# Exasol Virtual Schema Common 4.0.0, released 2020-11-??

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