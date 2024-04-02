# Exasol Virtual Schema 8.0.0, released 2024-04-02

Code name: Charset is always `utf-8`, deprecated IMPORT_DATA_TYPES `FROM_RESULT_SET` value

## Summary

The behaviour when it comes to character sets is now simplified,
The target charset is now always UTF-8.

The `IMPORT_DATA_TYPES` property (and value `FROM_RESULT_SET`) are now deprecated (change in vs-common-jdbc):
An exception will be thrown when users use`FROM_RESULT_SET`. The exception message warns the user that the value is no longer supported and the property itself is also deprecated.

Using timestamps with local timezone in the Exasol virtual schema now returns a proper timestamp with local timezone.

Querying char and varchar datatypes in LOCAL mode are now returned with UTF8 characterset, as expected.

## Refactoring

* #105: Updated tests to include Exasol V8/ Update to vsjdbc 12.0.0

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:virtual-schema-common-jdbc:11.0.2` to `12.0.0`

### Test Dependency Updates

* Added `com.exasol:udf-debugging-java:0.6.11`
* Updated `com.exasol:virtual-schema-common-jdbc:11.0.2` to `12.0.0`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.3.1` to `2.0.0`
* Updated `com.exasol:project-keeper-maven-plugin:3.0.1` to `4.1.0`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.11.0` to `3.12.1`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.2.3` to `3.2.5`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.4.1` to `3.6.3`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.2.3` to `3.2.5`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.5.0` to `1.6.0`
