# Exasol Virtual Schema 6.0.2, released 2022-02-07

Code name: Fix push-down of `CURRENT_CLUSTER` function

## Summary

This release fixes execution of the `CURRENT_CLUSTER` function with the next Exasol release. Exasol 8 will push down this function. This requires that the virtual schema omits the parenthesis in the function call.

## Bugfixes

* #63: Removed parentheses for CURRENT_CLUSTER function

## Dependency Updates

### Plugin Dependency Updates

* Updated `org.sonatype.ossindex.maven:ossindex-maven-plugin:3.1.0` to `3.2.0`
