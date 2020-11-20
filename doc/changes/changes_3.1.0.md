# Exasol Virtual Schema Common 3.1.0, released 2020-11-17

Code name: Security Update

## Summary

Classification: High
Please update your adapters as soon as possible!
This release fixes several SQL injection vulnerabilities on the remote database of the virtual schema. 
The local Exasol database defining the virtual schema is not affected.

This release also provides support for new capabilities: 

FN_BIT_LROTATE
FN_BIT_RROTATE
FN_BIT_LSHIFT
FN_BIT_RSHIFT
FN_FROM_POSIX_TIME
FN_HOUR
FN_INITCAP
FN_AGG_EVERY
FN_AGG_SOME
FN_AGG_MUL_DISTINCT
FN_HASHTYPE_MD5
FN_HASHTYPE_SHA1
FN_HASHTYPE_SHA256
FN_HASHTYPE_SHA512
FN_HASHTYPE_TIGER
FN_MIN_SCALE
FN_AGG_LISTAGG
FN_AGG_LISTAGG_DISTINCT
FN_AGG_LISTAGG_SEPARATOR
FN_AGG_LISTAGG_ON_OVERFLOW_ERROR
FN_AGG_LISTAGG_ON_OVERFLOW_TRUNCATE
FN_AGG_LISTAGG_ORDER_BY
FN_AGG_COUNT_TUPLE

## Bugs

* #30: Fixed a CVE-2020-15250 in a transitive dependency.

## Features / Enhancements

* #16: Get interval precision from system table.

## Documentation

* #15: Improved the documentation.

## Refactoring

* #18: Use test-db-builder for integration tests.
* #22: Updated to the new major version of `virtual-schema-common-jdbc`.
* #26: Updated to the new major version of `virtual-schema-common-jdbc`.

## Dependency updates

* Added com.exasol:test-db-builder-java:1.1.0
* Added com.exasol:artifact-reference-checker-maven-plugin:0.3.1
* Added org.junit.jupiter:junit-jupiter:5.7.0
* Updated com.exasol:exasol-testcontainers:jar:2.0.3 to version 3.3.1
* Updated com.exasol:exasol-jdbc:6.2.5 to version 7.0.3
* Updated com.exasol:virtual-schema-common-jdbc:5.0.4 to version 7.0.0
* Updated com.exasol:hamcrest-resultset-matcher:1.1.1 to version 1.2.1
* Updated org.mockito:mockito-junit-jupiter:3.3.3 to version 3.6.0
* Updated org.testcontainers:junit-jupiter:1.14.3 to version 1.15.0
* Removed org.junit.jupiter:junit-jupiter-engine
* Removed org.junit.jupiter:junit-jupiter-params
* Removed org.junit.platform:junit-platform-runner