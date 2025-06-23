package com.exasol.adapter.dialects.exasol;

import java.sql.Connection;

import com.exasol.ExaMetadata;
import com.exasol.adapter.AdapterProperties;
import com.exasol.adapter.dialects.BaseIdentifierConverter;
import com.exasol.adapter.dialects.IdentifierConverter;
import com.exasol.adapter.jdbc.AbstractRemoteMetadataReader;
import com.exasol.adapter.jdbc.BaseTableMetadataReader;
import com.exasol.adapter.jdbc.ColumnMetadataReader;
import com.exasol.adapter.jdbc.TableMetadataReader;

/**
 * This class reads Exasol-specific database metadata.
 */
public class ExasolMetadataReader extends AbstractRemoteMetadataReader {
    /**
     * Create a new instance of the {@link ExasolMetadataReader}.
     *
     * @param connection database connection through which the reader retrieves the metadata from the remote source
     * @param properties user-defined properties
     * @param exaMetadata Metadata of the Exasol database
     */
    public ExasolMetadataReader(final Connection connection, final AdapterProperties properties, ExaMetadata exaMetadata) {
        super(connection, properties, exaMetadata);
    }

    @Override
    protected ColumnMetadataReader createColumnMetadataReader() {
        return new ExasolColumnMetadataReader(this.connection, this.properties, this.exaMetadata, getIdentifierConverter());
    }

    @Override
    protected TableMetadataReader createTableMetadataReader() {
        return new BaseTableMetadataReader(this.connection, this.columnMetadataReader, this.properties,
                this.exaMetadata, this.identifierConverter);
    }

    @Override
    protected IdentifierConverter createIdentifierConverter() {
        return BaseIdentifierConverter.createDefault();
    }
}