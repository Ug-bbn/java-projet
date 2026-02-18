package com.sgpa.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static HikariDataSource dataSource;

    static {
        Properties dbProperties = new Properties();
        try (InputStream input = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input == null) {
                InputStream input2 = DatabaseConnection.class.getResourceAsStream("/database.properties");
                if (input2 != null) {
                    dbProperties.load(input2);
                    input2.close();
                    logger.info("Proprietes de base de donnees chargees");
                } else {
                    logger.error("Impossible de charger database.properties");
                }
            } else {
                dbProperties.load(input);
                logger.info("Proprietes de base de donnees chargees avec succes");
            }

            String url = dbProperties.getProperty("db.url");
            String username = dbProperties.getProperty("db.username");
            String password = dbProperties.getProperty("db.password");

            if (url != null && username != null && password != null) {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(url);
                config.setUsername(username);
                config.setPassword(password);
                config.setMaximumPoolSize(10);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(30000);
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);

                dataSource = new HikariDataSource(config);
                logger.info("Pool de connexions HikariCP initialise");

                createTablesIfNotExist();
            } else {
                logger.error("Proprietes de connexion manquantes dans database.properties");
            }
        } catch (IOException | SQLException e) {
            logger.error("Erreur lors de l'initialisation de la base de donnees", e);
        }
    }

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Le pool de connexions n'a pas ete initialise");
        }
        return dataSource.getConnection();
    }

    public static Connection getInstance() throws SQLException {
        return getConnection();
    }

    private static void createTablesIfNotExist() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            try (InputStream is = DatabaseConnection.class.getResourceAsStream("/db/migration/schema.sql")) {
                if (is != null) {
                    String schemaSql = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    for (String sql : schemaSql.split(";")) {
                        if (!sql.trim().isEmpty()) {
                            stmt.execute(sql);
                        }
                    }
                    logger.info("Schema base de donnees verifie/mis a jour.");
                } else {
                    logger.error("Fichier schema.sql introuvable.");
                }
            } catch (IOException e) {
                logger.error("Erreur lors de la lecture du schema SQL", e);
            }

            createDefaultAdminIfNeeded(conn);
        }
    }

    private static void createDefaultAdminIfNeeded(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) as count FROM utilisateurs WHERE role = 'ADMIN'";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             ResultSet rs = checkStmt.executeQuery()) {

            if (rs.next() && rs.getInt("count") == 0) {
                String adminPasswordHash = BCrypt.hashpw("admin", BCrypt.gensalt());

                String insertSql = "INSERT INTO utilisateurs (username, password_hash, nom, prenom, role, date_creation) "
                        + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, "admin");
                    insertStmt.setString(2, adminPasswordHash);
                    insertStmt.setString(3, "Administrateur");
                    insertStmt.setString(4, "Systeme");
                    insertStmt.setString(5, "ADMIN");
                    insertStmt.executeUpdate();
                }
                logger.info("Compte administrateur par defaut cree (admin/admin)");
            }
        }
    }
}
