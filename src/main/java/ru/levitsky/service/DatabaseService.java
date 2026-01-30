package ru.levitsky.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

/**
 * PostgreSQL database service.
 * Provides methods to execute SQL statements and retrieve table metadata.
 */
public class DatabaseService {

    //TODO AutoCloseable
    //TODO add logger

    final Connection connection;

    /**
     * Initializes a new database connection.
     *
     * @param url      JDBC URL of the database
     * @param user     database username
     * @param password database password
     * @throws Exception if connection cannot be established
     */
    public DatabaseService(String url, String user, String password) throws Exception {
        this.connection = DriverManager.getConnection(url, user, password);
    }

    /**
     * Executes an arbitrary SQL statement (DDL or DML).
     *
     * @param sql SQL statement
     * @throws SQLException if an error occurs while executing the statement
     */
    public void execute(String sql) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        }
    }

    /**
     * Retrieves the set of column names for a given table.
     *
     * @param table table name
     * @return set of column names
     * @throws SQLException if an error occurs while querying the database
     */
    public Set<String> getColumns(String table) throws SQLException {
        Set<String> cols = new HashSet<>();

        String sql = """
                SELECT column_name
                FROM information_schema.columns
                WHERE table_name = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, table);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                cols.add(rs.getString(1));
            }
        }
        return cols;
    }
}
