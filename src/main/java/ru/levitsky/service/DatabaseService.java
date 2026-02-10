package ru.levitsky.service;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import ru.levitsky.config.DataSourceConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

@Singleton
public class DatabaseService {

    final Connection connection;

    public DatabaseService(DataSourceConfig config) throws Exception {
        this.connection = DriverManager.getConnection(
                config.getUrl(),
                config.getUsername(),
                config.getPassword()
        );
    }

    public void execute(String sql) throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        }
    }

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

    @PreDestroy
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
