# Exasol Virtual Schema 7.1.3, released 2023-07-06

Code name: Refactoring

## Summary

This release replaces Adapter Property `IS_LOCAL` from `virtual-schema-common-java` by a constant in the current project as this property is specific to Exasol Virtual Schema.

See also ticket [VSCOMJAVA #273](https://github.com/exasol/virtual-schema-common-java/pull/273) removing the adapter property from `virtual-schema-common-java`.

## Refactoring

* #85: Replaced adapter property `IS_LOCAL` from virtual-schema-common-java by a constant in VSEXA.
## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:virtual-schema-common-jdbc:10.5.0` to `11.0.0`

### Test Dependency Updates

* Updated `com.exasol:virtual-schema-common-jdbc:10.5.0` to `11.0.0`

### Plugin Dependency Updates

* Updated `com.exasol:project-keeper-maven-plugin:2.9.7` to `2.9.8`
