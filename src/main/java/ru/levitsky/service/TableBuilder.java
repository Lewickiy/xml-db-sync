package ru.levitsky.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TableBuilder {

    private final XmlParser parser;

    public TableBuilder(XmlParser parser) {
        this.parser = parser;
    }

    public List<String> getColumnNames(String tableName) {
        List<Map<String, String>> rows = parser.getRows(tableName);
        Set<String> columns = new LinkedHashSet<>();

        for (Map<String, String> row : rows) {
            columns.addAll(row.keySet());
        }

        if (!parser.hasParams(tableName)) {
            columns.remove("params");
        }

        return new ArrayList<>(columns);
    }

    /**
     * Builds a {@code CREATE TABLE} DDL statement for the specified XML entity.<br><br>
     * <p>
     * The table structure is derived dynamically from the XML data:<br>
     * - Each discovered XML element or attribute becomes a {@code TEXT} column<br>
     * - If the XML contains {@code <param>} elements, a {@code params} column of type {@code JSONB} is added<br>
     * - The {@code vendorcode} column, if present, is marked as {@code UNIQUE}
     * and is later used for UPSERT operations<br><br>
     * <p>
     * The table is created only if it does not already exist
     * ({@code CREATE TABLE IF NOT EXISTS}).
     *
     * @param tableName the table name (corresponds to the XML node name)
     * @return the SQL DDL statement used to create the table
     */
    public String getTableDDL(String tableName) {
        List<String> columns = getColumnNames(tableName);
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE IF NOT EXISTS \"").append(tableName).append("\" (\n");

        for (int i = 0; i < columns.size(); i++) {
            String col = columns.get(i);
            if ("params".equals(col)) {
                ddl.append("  \"").append(col).append("\" JSONB");
            } else {
                ddl.append("  \"").append(col).append("\" TEXT");
            }

            if ("vendorcode".equals(col)) {
                ddl.append(" UNIQUE");
            }

            if (i < columns.size() - 1) ddl.append(",");
            ddl.append("\n");
        }

        ddl.append(");");
        return ddl.toString();
    }

    public List<String> getAddColumnStatements(String tableName, Set<String> existingColumns) {
        List<String> statements = new ArrayList<>();
        for (String col : getColumnNames(tableName)) {
            if (!existingColumns.contains(col)) {
                String type = "params".equals(col) ? "JSONB" : "TEXT";
                statements.add("ALTER TABLE \"" + tableName + "\" ADD COLUMN \"" + col + "\" " + type);
            }
        }
        return statements;
    }
}
