# Exasol SQL Dialect

Connecting to an Exasol database is the simplest way to get started with Virtual Schemas.
You don't have to install any JDBC driver, because it is already installed in the Exasol database and also included in the JAR of the JDBC adapter.

## Installing the Adapter Script

Upload the latest available release of [Exasol Virtual Schema](https://github.com/exasol/exasol-virtual-schema/releases) to Bucket FS.

Then create a schema to hold the adapter script.

```sql
CREATE SCHEMA SCHEMA_FOR_VS_SCRIPT;
```

The SQL statement below creates the adapter script, defines the Java class that serves as entry point and tells the UDF framework where to find the libraries (JAR files) for Virtual Schema and database driver.

```sql
CREATE JAVA ADAPTER SCRIPT SCHEMA_FOR_VS_SCRIPT.ADAPTER_SCRIPT_EXASOL AS
    %scriptclass com.exasol.adapter.RequestDispatcher;
    %jar /buckets/<BFS service>/<bucket>/virtual-schema-dist-6.0.0-exasol-3.1.1.jar;
/
```

### Defining a Named Connection

Define the connection to the other Exasol cluster as shown below.

```sql
CREATE CONNECTION EXASOL_CONNECTION 
TO 'jdbc:exa:<host>:<port>' 
USER '<user>' 
IDENTIFIED BY '<password>';
```

## Using IMPORT FROM EXA Instead of IMPORT FROM JDBC

Exasol provides the faster and parallel `IMPORT FROM EXA` command for loading data from Exasol. You can tell the adapter to use this command instead of `IMPORT FROM JDBC` by setting the `IMPORT_FROM_EXA` property. 
In this case you have to provide the additional `EXA_CONNECTION_STRING` which is the connection string used for the internally used `IMPORT FROM EXA` command (it also supports ranges like `192.168.6.11..14:8563`). Please note, that the `CONNECTION` object must still have the JDBC connection string in `AT`, because the Adapter Script uses a JDBC connection to obtain the metadata when a schema is created or refreshed. 
For the internally used `IMPORT FROM EXA` statement, the address from `EXA_CONNECTION_STRING` and the user name and password from the connection will be used.

### Creating a Virtual Schema

```sql
CREATE VIRTUAL SCHEMA VIRTUAL_EXASOL 
    USING SCHEMA_FOR_VS_SCRIPT.ADAPTER_SCRIPT_EXASOL WITH
    SQL_DIALECT     = 'EXASOL'
    CONNECTION_NAME = 'EXASOL_CONNECTION'
    SCHEMA_NAME     = '<schema name>'
    IMPORT_FROM_EXA = 'true'
    EXA_CONNECTION_STRING = '<host>:<port>';
```

## Using IMPORT FROM JDBC

### Creating a Virtual Schema

```sql
CREATE VIRTUAL SCHEMA <virtual schema name> 
    USING SCHEMA_FOR_VS_SCRIPT.ADAPTER_SCRIPT_EXASOL WITH
    WITH
    SQL_DIALECT     = 'EXASOL'
    CONNECTION_NAME = 'EXASOL_CONNECTION'
    SCHEMA_NAME     = '<schema name>';
```

## Known limitations

* Using literals and constant expressions with datatype `TIMESTAMP WITH LOCAL TIME ZONE` in Virtual Schemas 
can produce an incorrect results. We recommend using 'TIMESTAMP' instead. If you are willing to take the risk
and want to use `TIMESTAMP WITH LOCAL TIME ZONE` anyway, please, create a Virtual Schema with the following
additional property `IGNORE_ERRORS = 'TIMESTAMP_WITH_LOCAL_TIME_ZONE_USAGE'`. 
We also recommend to set Exasol system `time_zone` to UTC while working with `TIMESTAMP WITH LOCAL TIME ZONE`.

## Supported Capabilities

The Exasol SQL dialect supports all capabilities that are supported by the virtual schema framework.

## Connection Types

You can use different connection options depending on a type of source Exasol database.

### Data Source a Remote Exasol Instance or Cluster

Add the following parameters to `CREATE VIRTUAL SCHEMA`:

    IMPORT_FROM_EXA = 'true'
    EXA_CONNECTION_STRING = '<host-or-range>:<port>'

You additionally need to provide a named connection with JDBC details so that Virtual Schema can read the metadata.

#### Data Source is the Same Exasol Instance or Cluster Virtual Schemas Runs on

In this case the best possible connection type is a so called "local" connection.

Add the following parameters to `CREATE VIRTUAL SCHEMA`:

    IS_LOCAL = 'true'

The parameter `IS_LOCAL` provides an additional speed-up in this particular use case. 
The way this works is that Virtual Schema generates a regular `SELECT` statement instead of an `IMPORT` statement. 
And that `SELECT` can be directly executed by the core database, whereas the `IMPORT` statement takes a detour via the ExaLoader.

#### Data Source is an Exasol Instance or Cluster Only Reachable via JDBC

While this connection type works, it is also the slowest option and exists mainly to support integration tests on the ExaLoader. 
We recommend that you use `IMPORT FROM EXA` instead.
