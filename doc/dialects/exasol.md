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
    %jar /buckets/<BFS service>/<bucket>/virtual-schema-dist-8.0.0-exasol-4.0.0.jar;
/
```

### Defining a Named Connection

Define the connection to the other Exasol cluster as shown below.

```sql
CREATE CONNECTION JDBC_CONNECTION 
TO 'jdbc:exa:<host>:<port>' 
USER '<user>' 
IDENTIFIED BY '<password>';
```

You can learn more about [defining named connections](https://docs.exasol.com/sql/create_connection.htm) in the Exasol online handbook.

## Choosing the Type of Connection

You have three options to pick from when connecting to an Exasol instance or cluster. The options are explained below.

### Using IMPORT FROM EXA

Exasol provides the faster and parallel `IMPORT FROM EXA` command for loading data from Exasol. You can tell the adapter to use this command instead of `IMPORT FROM JDBC` by setting the `IMPORT_FROM_EXA` property. 

In this case you have to provide the additional `EXA_CONNECTION` which contains the name of the connection definition used for the internally used `IMPORT FROM EXA` command.

That means that in this case you will have **two** named connections: a JDBC connection for the Virtual Schema adapter and an EXA connection for the EXALoader &mdash; which runs the `IMPORT`.

Please refer to the [CREATE CONNECTION](https://docs.exasol.com/sql/create_connection.htm) documentation for more details about how to define an EXA connection.

#### Creating a Virtual Schema With EXA Import

```sql
CREATE CONNECTION EXA_CONNECTION
TO '<host-or-list>:<port>'
USER '<user>'
PASSWORD '<password>'
```

```sql
CREATE VIRTUAL SCHEMA VIRTUAL_EXASOL 
USING SCHEMA_FOR_VS_SCRIPT.ADAPTER_SCRIPT_EXASOL WITH
    SQL_DIALECT     = 'EXASOL'
    CONNECTION_NAME = 'JDBC_CONNECTION'
    SCHEMA_NAME     = '<schema name>'
    IMPORT_FROM_EXA = 'true'
    EXA_CONNECTION  = 'EXA_CONNECTION';
```

### Using IMPORT FROM JDBC

Should the EXA connection not be an option for you, you can alternatively use a regular JDBC connection for the `IMPORT`. Note that this option is slower because it lacks the parallelization the `IMPORT FROM EXA` variant.

#### Creating a Virtual Schema With JDBC Import

```sql
CREATE VIRTUAL SCHEMA <virtual schema name> 
USING SCHEMA_FOR_VS_SCRIPT.ADAPTER_SCRIPT_EXASOL WITH
    WITH
    SQL_DIALECT     = 'EXASOL'
    CONNECTION_NAME = 'JDBC_CONNECTION'
    SCHEMA_NAME     = '<schema name>';
```

### Using IS_LOCAL

If the data source is the same Exasol instance or cluster Virtual Schemas runs on, then the best possible connection type is a so called "local" connection.

Add the following parameter to `CREATE VIRTUAL SCHEMA`:

    IS_LOCAL = 'true'

The `IS_LOCAL` parameter provides an additional speed-up in this particular use case. 

The way this works is that Virtual Schema generates a regular `SELECT` statement instead of an `IMPORT` statement. 

And that `SELECT` can be directly executed by the core database, whereas the `IMPORT` statement takes a detour via the ExaLoader.

**Important:** Please note that since the generated `SELECT` command runs with the permissions of the owner of the Virtual Schema, that user must have privileges to access what you plan to select!

`IMPORT` statements use a connection definition which allows connecting with a different user account. Generated `SELECT` statements do not open additional connections (hence the "local" moniker) so they inherit the context of the Virtual Schema query they are executed in &mdash; including permissions.

#### Creating a Local Virtual Schema

```sql
CREATE VIRTUAL SCHEMA <virtual schema name> 
USING SCHEMA_FOR_VS_SCRIPT.ADAPTER_SCRIPT_EXASOL WITH
    WITH
    SQL_DIALECT     = 'EXASOL'
    CONNECTION_NAME = 'JDBC_CONNECTION'
    SCHEMA_NAME     = '<schema name>';
    IS_LOCAL        = 'true'
```

Note that you still need to provide a JDBC connection. This is used by the Virtual Schema adapter internally. It is not used for mass data transfer though. And that is where the performance gain comes from.

## Supported Capabilities

The Exasol SQL dialect supports all capabilities that are supported by the virtual schema framework.

## Known limitations

* Using literals and constant expressions with `TIMESTAMP WITH LOCAL TIME ZONE` data type in Virtual Schemas can produce an incorrect results.
   * We recommend using `TIMESTAMP` instead.
   * If you are willing to take the risk and want to use `TIMESTAMP WITH LOCAL TIME ZONE` anyway, please, create a Virtual Schema with the following additional property `IGNORE_ERRORS = 'TIMESTAMP_WITH_LOCAL_TIME_ZONE_USAGE'`.
   * We also recommend to set Exasol system `time_zone` to UTC while working with `TIMESTAMP WITH LOCAL TIME ZONE`.
