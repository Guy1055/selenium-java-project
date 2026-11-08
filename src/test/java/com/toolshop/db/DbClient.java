package com.toolshop.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PLACEHOLDER TIER. The public demo exposes no database, so these checks only run against a
 * self-hosted Toolshop (its docker-compose ships MariaDB). Point DB_URL, API_URL and BASE_URL there.
 */
public final class DbClient implements AutoCloseable {
    private final Connection connection;

    public DbClient(String url) throws SQLException {
        this.connection = DriverManager.getConnection(url);
    }

    public List<Map<String, Object>> query(String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            try (ResultSet results = statement.executeQuery()) {
                ResultSetMetaData meta = results.getMetaData();
                List<Map<String, Object>> rows = new ArrayList<>();
                while (results.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int column = 1; column <= meta.getColumnCount(); column++) {
                        row.put(meta.getColumnLabel(column), results.getObject(column));
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }
}
