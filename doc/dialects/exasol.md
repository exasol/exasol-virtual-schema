# Exasol SQL Dialect

Connecting to an Exasol database is the simplest way to get started with Virtual Schemas. You don't have to install any JDBC driver, because it is already installed in the Exasol database and also included in the JAR of the JDBC adapter.

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
    %jar /buckets/<BFS service>/<bucket>/virtual-schema-dist-12.0.0-exasol-8.0.0.jar;
/
```

### Defining a Named Connection

Define the connection to the other Exasol cluster as shown below:

```sql
CREATE CONNECTION JDBC_CONNECTION
TO 'jdbc:exa:<host>:<port>'
USER '<user>'
IDENTIFIED BY '<password>';
```

For connecting via TLS you can specify the certificate's fingerprint in the JDBC URL like this:

```sql
CREATE CONNECTION JDBC_CONNECTION
TO 'jdbc:exa:<host>/<fingerprint>:<port>'
USER '<user>'
IDENTIFIED BY '<password>';
```

You can learn more about [defining named connections](https://docs.exasol.com/sql/create_connection.htm) in the Exasol online handbook.

## Choosing the Type of Connection

You have three options to pick from when connecting to an Exasol instance or cluster. The options are explained below.

### Using `IMPORT FROM EXA`

Exasol provides the faster and parallel `IMPORT FROM EXA` command for loading data from another Exasol instance. You can tell the adapter to use this command instead of `IMPORT FROM JDBC` by setting the `IMPORT_FROM_EXA` property.

In this case you have to provide the additional `EXA_CONNECTION` which contains the name of the connection definition used for the internally used `IMPORT FROM EXA` command.

That means you will have **two** named connections: a JDBC connection and an EXA connection. The Virtual Schema adapter uses the JDBC connection for reading metadata. The EXA connection is used by the EXALoader that runs the `IMPORT` statement.

Please refer to the [CREATE CONNECTION](https://docs.exasol.com/sql/create_connection.htm) documentation for more details about how to define an EXA connection.

#### Creating a Virtual Schema With EXA Import

```sql
CREATE CONNECTION EXA_CONNECTION
TO '<host-or-list>:<port>'
USER '<user>'
IDENTIFIED BY '<password>'
```

With Exasol 7.1.0 and later you can specify the TLS certificate's fingerprint:

```sql
CREATE CONNECTION EXA_CONNECTION
TO '<host-or-list>/<fingerprint>:<port>'
USER '<user>'
IDENTIFIED BY '<password>'
```

```sql
CREATE VIRTUAL SCHEMA VIRTUAL_EXASOL
USING SCHEMA_FOR_VS_SCRIPT.ADAPTER_SCRIPT_EXASOL WITH
    CONNECTION_NAME = 'JDBC_CONNECTION'
    SCHEMA_NAME     = '<schema name>'
    IMPORT_FROM_EXA = 'true'
    EXA_CONNECTION  = 'EXA_CONNECTION';
```

#### Map Datatypes With EXA Import

Unlike for a JDBC connection `IMPORT FROM EXA` does not use an explicit datatype mapping. In consequence columns of type `HASHTYPE` are mapped to `VARCHAR` and joining such a column therefore failed in Exasol 7.1 with error message `Feature not supported: Incomparable Types: VARCHAR(32) UTF8 and HASHTYPE(16 BYTE)!`.

Exasol Virtual Schema in version 7.2.0 and later mitigates this by offering parameter `GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA` with values `true` and `false` (default):

```sql
CREATE VIRTUAL SCHEMA VIRTUAL_EXASOL
USING SCHEMA_FOR_VS_SCRIPT.ADAPTER_SCRIPT_EXASOL WITH
    CONNECTION_NAME                        = 'JDBC_CONNECTION'
    SCHEMA_NAME                            = '<schema name>'
    IMPORT_FROM_EXA                        = 'true'
    EXA_CONNECTION                         = 'EXA_CONNECTION'
    GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA = 'true';
```

This will add explicit datatype mapping to the generated command when using `IMPORT FROM EXA`.

Example for the generated pushdown query with `GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA = 'false'` (default):

```sql
IMPORT FROM EXA AT "EXA_CONNECTION" STATEMENT '...'
```

Pushdown query with `GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA = 'true'`:

```sql
IMPORT INTO (c1 DECIMAL(36,1), c2 .... ) FROM EXA AT "EXA_CONNECTION" STATEMENT '...'
```

##### Data type mismatch

In case you run into a `Data type mismatch` issue which looks like this:
`Adapter generated invalid pushdown query for virtual table <TABLENAME>: Data type mismatch in column number <COLUMN NUMBER>  (1-indexed).Expected <EXPECTED IMPORT TYPE>, but got <IMPORT TYPE>.`

You can set the datatype mapping to true: `GENERATE_JDBC_DATATYPE_MAPPING_FOR_EXA = 'true'`. 
This will usually solve the issue by providing type hints.

### Using `IMPORT FROM JDBC`

You can alternatively use a regular JDBC connection for the `IMPORT`. Note that this option is slower because it lacks the parallelization the `IMPORT FROM EXA` variant.

#### Creating a Virtual Schema With JDBC Import

```sql
CREATE VIRTUAL SCHEMA <virtual schema name>
USING SCHEMA_FOR_VS_SCRIPT.ADAPTER_SCRIPT_EXASOL WITH
    CONNECTION_NAME = 'JDBC_CONNECTION'
    SCHEMA_NAME     = '<schema name>';
```

### Using `IS_LOCAL`

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
    CONNECTION_NAME = 'JDBC_CONNECTION'
    SCHEMA_NAME     = '<schema name>'
    IS_LOCAL        = 'true';
```

Note that you still need to provide a JDBC connection. This is used by the Virtual Schema adapter internally. It is not used for mass data transfer though. And that is where the performance gain comes from.

## Transport Layer Security (TLS)

With version 7.1 Exasol introduced TLS encryption on the database port. Other ports were TLS capable before that already.

### Using TLS Connections

To establish a TLS connection with an Exasol Virtual Schema, you must use Exasol Virtual Schema 5.0.4 or later and connect to a compatible Exasol server version (see section ["Exasol Server Versions and TLS Support"](#exasol-server-versions-and-tls-support)).

The reason why you need the Exasol Virtual Schema 5.0.4 or later is that with this version we built-in a JDBC driver that is TLS-capable.

Check the [JDBC driver documentation](https://docs.exasol.com/connect_exasol/drivers/jdbc.htm) in our online handbook for details.

### Exasol Server Versions and TLS Support

The following Exasol Server Versions support TLS connections via JDBC:

* 6.2.15 and later 6.2.x versions
* 7.0.10 and later 7.0.x versions
* All versions from 7.1.x on

Older versions (6.2.0 &hellip; 6.2.14 and 7.0.0 &hellip; 7.0.9) automatically fall back to legacy encryption. These versions are outdated anyway. If you still have one of them running, you should still disable TLS support explicitly by specifying parameter `legacyencryption=1` in the JDBC URL (see details about [supported driver properties](https://docs.exasol.com/db/latest/connect_exasol/drivers/jdbc.htm#SupportedDriverProperties)). This is a visual reminder that TLS won't work with those versions.

If you are interested, you can find even more detailed information in our [roadmap ticket "TLS for all Exasol Drivers"](https://www.exasol.com/support/browse/EXASOL-2936).

### Disabling TLS

If you want to connect to a cluster running Exasol 7.0.x or earlier with a recent Exasol Virtual Schema (5.0.4 or later), you _should_ explicitly disable TLS on in the JDBC connection the Virtual Schema uses, otherwise the driver will attempt to establish a TLS connection to the server that does not support it and the connection attempt will fail.

The [JDBC driver property `legacyencryption`](https://docs.exasol.com/connect_exasol/drivers/jdbc.htm#SupportedDriverProperties) switches between TLS and the encryption scheme of older Exasol versions. Set it to `1` to disable TLS.

Example:

```sql
CREATE CONNECTION LEGACY_JDBC_CONNECTION
TO 'jdbc:exa:<host>:<port>;legacyencryption=1'
USER '<user>'
IDENTIFIED BY '<password>';
```

## Supported Capabilities

The Exasol SQL dialect supports all capabilities that are supported by the virtual schema framework.

## Known Limitations

### Data type `TIMESTAMP WITH LOCAL TIME ZONE`

Using literals and constant expressions with `TIMESTAMP WITH LOCAL TIME ZONE` data type in Virtual Schemas can produce incorrect results.
* We recommend using `TIMESTAMP` instead.
* If you are willing to take the risk and want to use `TIMESTAMP WITH LOCAL TIME ZONE` anyway, please, create a Virtual Schema with the following additional property `IGNORE_ERRORS = 'TIMESTAMP_WITH_LOCAL_TIME_ZONE_USAGE'`.
* We also recommend to set Exasol system `time_zone` to UTC while working with `TIMESTAMP WITH LOCAL TIME ZONE`.

### Clause `ORDER BY`

Clause `ORDER BY` can be used without limitations when using [local connection](#using-is_local).

Connections [EXA](#using-import-from-exa) and [JDBC](#using-import-from-jdbc) are using `IMPORT` which only supports unordered data transfer. Therefore the outermost order of the imported result rows is not guaranteed.

If you need ordering then please
* apply the ordering on top-level in the Exasol target database outside the virtual schema,
* use a sub-query to access the virtual schema and
* annotate the sub-query with `ORDER BY FALSE` to prevent push down of the top-level `ORDER BY`, e.g.

```sql
SELECT * FROM (<virtual-schema-query> ORDER BY FALSE) ORDER BY <criteria> [, ...]
```

See also [General Known Limitations](https://docs.exasol.com/db/latest/database_concepts/virtual_schemas.htm#KnownLimitations) in official Exasol documentation on Virtual Schemas.
