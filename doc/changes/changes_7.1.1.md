# Exasol Virtual Schema 7.1.1, released 2023-??-??

Code name:

## Summary

This release adds a test for escaping wildcard when retrieving table metadata from JDBC driver and removes local classes in favor of those provided by version 10.4.0 of VSCJDBC.


## Documentation

* #94: Added test for Add test for escaping wildcard when retrieving table metadata from JDBC driver.
* #95: Removed duplicated classes

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:virtual-schema-common-jdbc:10.2.0` to `10.4.0`

### Test Dependency Updates

* Updated `com.exasol:virtual-schema-common-jdbc:10.2.0` to `10.4.0`

### Plugin Dependency Updates

* Updated `com.exasol:project-keeper-maven-plugin:2.9.3` to `2.9.4`
* Updated `org.apache.maven.plugins:maven-deploy-plugin:3.0.0` to `3.1.0`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.1.0` to `3.2.1`
