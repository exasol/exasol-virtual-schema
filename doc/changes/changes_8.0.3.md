# Exasol Virtual Schema 8.0.3, released 2025-11-25

Code name: Update virtual-schema-common-jdbc dependency

## Summary

This release updates virtual-schema-common-jdbc to propagate the fix in underlying libraries.

Fixed issues:
1. When applying CAST on the results of COUNT(*) of the empty table from the Virtual Scheme, it returns Null instead of 0.
2. Query from empty virtual schema table returns empty result (instead of expected 1): `SELECT 1 FROM VST.TEST1 HAVING  max(COL1) is null;`

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:virtual-schema-common-jdbc:13.0.0` to `13.0.1`

### Test Dependency Updates

* Updated `com.exasol:virtual-schema-common-jdbc:13.0.0` to `13.0.1`
* Updated `org.jacoco:org.jacoco.agent:0.8.13` to `0.8.14`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:2.0.4` to `2.0.5`
* Updated `com.exasol:project-keeper-maven-plugin:5.2.3` to `5.4.3`
* Updated `com.exasol:quality-summarizer-maven-plugin:0.2.0` to `0.2.1`
* Updated `io.github.git-commit-id:git-commit-id-maven-plugin:9.0.1` to `9.0.2`
* Updated `org.apache.maven.plugins:maven-artifact-plugin:3.6.0` to `3.6.1`
* Updated `org.apache.maven.plugins:maven-clean-plugin:3.4.1` to `3.5.0`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.14.0` to `3.14.1`
* Updated `org.apache.maven.plugins:maven-dependency-plugin:3.8.1` to `3.9.0`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.5.0` to `3.6.2`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.5.3` to `3.5.4`
* Updated `org.apache.maven.plugins:maven-gpg-plugin:3.2.7` to `3.2.8`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.11.2` to `3.12.0`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.5.3` to `3.5.4`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.7.0` to `1.7.3`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.18.0` to `2.19.1`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.13` to `0.8.14`
* Updated `org.sonarsource.scanner.maven:sonar-maven-plugin:5.1.0.4751` to `5.2.0.4988`
* Updated `org.sonatype.central:central-publishing-maven-plugin:0.7.0` to `0.9.0`
