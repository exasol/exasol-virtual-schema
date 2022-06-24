# Exasol Virtual Schema 6.0.3, released 2022-02-??

Code name: Improved TLS documentation

## Summary

In version 6.0.3 we updated dependent versions and fixed typos in user guide.

The project version in the integration tests with the JAR package is now automatically determined.

This release upgrades dependencies and reduces the number of runtime dependencies, fixing CVE-2022-21724 in the PostgreSQL JDBC driver.

We also fixed the following vulnerabilities by updating other dependencies:

* CVE-2022-24823 (in `io.netty:netty-common`)
* CVE-2016-5003 (in `io.netty:netty-handler`)
* CVE-2016-5002 (in `io.netty:netty-handler`)
* CVE-2016-5004 (in `org.apache.xmlrpc:xmlrpc-client`)

## Bugfixes

* #74: Fixed security issue in dependency
* #64: Fixed typos in user guide

## Dependency Updates

### Test Dependency Updates

* Added `com.exasol:maven-project-version-getter:1.1.0`
* Updated `com.exasol:test-db-builder-java:3.2.2` to `3.3.0`
* Updated `org.slf4j:slf4j-jdk14:1.7.35` to `1.7.36`
