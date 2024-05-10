# Exasol Virtual Schema 7.1.3, released 2023-07-07

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
* Updated `org.jacoco:org.jacoco.agent:0.8.9` to `0.8.10`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.2.3` to `1.3.0`
* Updated `com.exasol:project-keeper-maven-plugin:2.9.7` to `2.9.9`
* Updated `org.apache.maven.plugins:maven-dependency-plugin:3.5.0` to `3.6.0`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.0.0` to `3.1.2`
* Updated `org.apache.maven.plugins:maven-gpg-plugin:3.0.1` to `3.1.0`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.0.0` to `3.1.2`
* Updated `org.basepom.maven:duplicate-finder-maven-plugin:1.5.1` to `2.0.1`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.4.1` to `1.5.0`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.15.0` to `2.16.0`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.9` to `0.8.10`
