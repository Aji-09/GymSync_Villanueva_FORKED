package com.group3.gymsync.database;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseInitializer {
    private static final String SCHEMA_RESOURCE = "/gymsync_db.sql";

    private DatabaseInitializer() {
    }

    public static void initialize() throws SQLException {
        initialize(new DatabaseConnection());
    }

    public static void initialize(DatabaseConnection databaseConnection) throws SQLException {
        String schema = loadSchema();
        try (Connection connection = databaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            for (String sql : schema.split(";")) {
                if (!sql.isBlank()) {
                    statement.execute(sql);
                }
            }
        }
    }

    private static String loadSchema() throws SQLException {
        try (InputStream schemaStream = DatabaseInitializer.class.getResourceAsStream(SCHEMA_RESOURCE)) {
            if (schemaStream != null) {
                return new String(schemaStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new SQLException("Unable to read the database schema resource.", e);
        }

        Path schemaPath = Path.of("database", "gymsync_db.sql");
        try {
            return Files.readString(schemaPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new SQLException("Unable to find the database schema at " + schemaPath.toAbsolutePath(), e);
        }
    }
}