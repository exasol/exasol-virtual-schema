# Exasol Virtual Schema 9.0.1, released 2026-06-??

Code name: Improvements

## Summary

This release addresses several issues found within Exasol-virtual-schema.

## Bugfixes

* #141: Refactor misleading `MandatoryProperty` class
* #142: Reused the injected Exasol dialect in local SQL generation
* #143: Cleaned up Exasol metadata handling and EXA query rewriting
* #144: Hardened Exasol column metadata mapping and dictionary lookup
* #137: Added integration tests for empty-group-by count and `HAVING max(...) is null` regressions

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:virtual-schema-common-jdbc:14.0.2` to `14.0.4`

### Test Dependency Updates

* Updated `com.exasol:virtual-schema-common-jdbc:14.0.2` to `14.0.4`
