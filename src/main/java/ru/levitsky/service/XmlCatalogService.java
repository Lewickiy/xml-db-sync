package ru.levitsky.service;

import jakarta.inject.Singleton;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
/**
 * Service for synchronizing XML data with a PostgreSQL database
 */
public class XmlCatalogService {

    private final TableBuilder tableBuilder;
    private final XmlParser parser;

    public XmlCatalogService(XmlParser xmlParser) throws Exception {
        this.parser = xmlParser;
        this.tableBuilder = new TableBuilder(parser);
    }

    /**
     * Returns the list of top-level XML entities (tables) present in the catalog.
     *
     * @return list of table names inferred from XML
     */
    public List<String> getTableNames() {
        return parser.getTableNames();
    }

    /**
     * Generates a {@code CREATE TABLE} DDL statement for a given table
     *
     * @param tableName the table name corresponding to an XML node
     * @return SQL DDL statement for creating the table
     */
    public String getTableDDL(String tableName) {
        return tableBuilder.getTableDDL(tableName);
    }

    /**
     * Synchronizes a database table with the XML data:<br>
     * - Creates the table if it does not exist<br>
     * - Adds missing columns dynamically<br>
     * - Upserts XML rows into the table<br>
     *
     * @param tableName the table to synchronize
     * @param db the {@link DatabaseService} to execute SQL statements
     * @throws Exception if any SQL or parsing error occurs
     */
    public void update(String tableName, DatabaseService db) throws Exception {
        db.execute(getTableDDL(tableName));

        Set<String> existingCols = db.getColumns(tableName);
        List<String> addCols = tableBuilder.getAddColumnStatements(tableName, existingCols);
        for (String sql : addCols) {
            db.execute(sql);
        }

        upsert(tableName, db);
    }

    /**
     * Performs UPSERT operations for a given table:<br>
     * - If {@code vendorcode} exists, performs ON CONFLICT DO UPDATE<br>
     * - Otherwise, performs standard INSERT<br>
     * - Columns of type JSONB (params) are handled via {@link PreparedStatement#setObject}
     *
     * @param tableName the table name
     * @param db the {@link DatabaseService} used to execute SQL statements
     * @throws Exception if database operations fail
     */
    private void upsert(String tableName, DatabaseService db) throws Exception {
        List<Map<String, String>> rows = parser.getRows(tableName);
        Set<String> dbCols = db.getColumns(tableName);
        boolean hasVendorCode = dbCols.contains("vendorcode");

        for (Map<String, String> row : rows) {
            String cols = row.keySet().stream().map(k -> "\"" + k + "\"").collect(Collectors.joining(","));
            String placeholders = row.keySet().stream().map(k -> "?").collect(Collectors.joining(","));

            String sql;
            if (hasVendorCode) {
                String updates = row.keySet().stream()
                        .filter(k -> !"vendorcode".equals(k))
                        .map(k -> "\"" + k + "\" = EXCLUDED.\"" + k + "\"")
                        .collect(Collectors.joining(","));
                sql = String.format(
                        "INSERT INTO \"%s\" (%s) VALUES (%s) ON CONFLICT (\"vendorcode\") DO UPDATE SET %s",
                        tableName, cols, placeholders, updates
                );
            } else {
                sql = String.format(
                        "INSERT INTO \"%s\" (%s) VALUES (%s)",
                        tableName, cols, placeholders
                );
            }

            try (PreparedStatement ps = db.connection.prepareStatement(sql)) {
                int i = 1;
                for (String col : row.keySet()) {
                    if ("params".equals(col)) {
                        ps.setObject(i++, row.get(col), java.sql.Types.OTHER);
                    } else {
                        ps.setString(i++, row.get(col));
                    }
                }
                ps.executeUpdate();
            }
        }
    }
}
