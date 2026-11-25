package com.group3.gymsync.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public final class DatabaseConnection {
    public static final String DATABASE_PATH_PROPERTY = "gymsync.database.path";

    private final Path databasePath;

    public DatabaseConnection() {
        String configuredPath = System.getProperty(DATABASE_PATH_PROPERTY);
        this.databasePath = configuredPath == null || configuredPath.isBlank()
                ? Path.of(System.getProperty("user.home"), "GymSync", "gymsync.db")
                : Path.of(configuredPath);
    }

    public DatabaseConnection(Path databasePath) {
        this.databasePath = Objects.requireNonNull(databasePath, "databasePath");
    }

    public Path getDatabasePath() {
        return databasePath;
    }

    public Connection getConnection() throws SQLException {
        Path absolutePath = databasePath.toAbsolutePath().normalize();
        Path parent = absolutePath.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new SQLException("Unable to create the database directory: " + parent, e);
            }
        }

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + absolutePath);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            return connection;
        } catch (SQLException e) {
            try {
                connection.close();
            } catch (SQLException closeException) {
                e.addSuppressed(closeException);
            }
            throw e;
        }
    }
}