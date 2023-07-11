# Exasol Virtual Schema 7.1.4, released 2023-??-??

Code name: Fix Issue With Integer Constants in `GROUP BY`

## Summary

This release fixes an issue with queries using `DISTINCT` with integer constants. The Exasol SQL processor turns `DISTINCT <integer>` into `GROUP BY <integer>` before push-down as an optimization. The adapter must not feed this back as Exasol interprets integers in `GROUP BY` clauses as column numbers which could lead to invalid results or the following error:

```
42000:Wrong column number. Too small value 0 as select list column reference in GROUP BY (smallest possible value is 1)
```

To fix this, Exasol VS now replaces integer constants in `GROUP BY` clauses with a constant string.

Please that you can still safely use `GROUP BY <column-number>` in your original query, since Exasol internally converts this to `GROUP BY "<column-name>"`, so that the virtual schema adapter can tell both situations apart.

## Bugfixes

* #108: Fixed issue with integer constants in `GROUP BY`

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:virtual-schema-common-jdbc:11.0.0` to `11.0.1`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.6.0` to `6.6.1`
* Updated `com.exasol:virtual-schema-common-jdbc:11.0.0` to `11.0.1`

### Plugin Dependency Updates

* Updated `org.apache.maven.plugins:maven-assembly-plugin:3.3.0` to `3.6.0`
