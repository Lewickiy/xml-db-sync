package ru.levitsky.service;

import jakarta.inject.Singleton;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class XmlCatalogService {

    private final TableBuilder tableBuilder;
    private final XmlParser parser;

    public XmlCatalogService(XmlParser xmlParser) throws Exception {
        this.parser = xmlParser;
        this.tableBuilder = new TableBuilder(parser);
    }

    public List<String> getTableNames() {
        return parser.getTableNames();
    }

    public String getTableDDL(String tableName) {
        return tableBuilder.getTableDDL(tableName);
    }

    public void update(String tableName, DatabaseService db) throws Exception {
        db.execute(getTableDDL(tableName));

        Set<String> existingCols = db.getColumns(tableName);
        List<String> addCols = tableBuilder.getAddColumnStatements(tableName, existingCols);
        for (String sql : addCols) {
            db.execute(sql);
        }

        upsert(tableName, db);
    }

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
