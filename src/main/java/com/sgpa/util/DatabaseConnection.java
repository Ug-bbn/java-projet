package com.sgpa.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Pool de connexions HikariCP ferme");
        }
    }

    private static void createTablesIfNotExist() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS medicaments (
                        id SERIAL PRIMARY KEY,
                        nom_commercial VARCHAR(100) NOT NULL,
                        principe_actif VARCHAR(100),
                        forme_galenique VARCHAR(50),
                        dosage VARCHAR(50),
                        prix_public DECIMAL(10,2),
                        necessite_ordonnance BOOLEAN DEFAULT FALSE,
                        seuil_min_alerte INTEGER
                    )
                """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS lots (
                        id SERIAL PRIMARY KEY,
                        medicament_id INTEGER REFERENCES medicaments(id) ON DELETE CASCADE,
                        numero_lot VARCHAR(50),
                        quantite_stock INTEGER,
                        date_peremption DATE,
                        prix_achat DECIMAL(10,2)
                    )
                """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS fournisseurs (
                        id SERIAL PRIMARY KEY,
                        nom VARCHAR(100) NOT NULL,
                        contact VARCHAR(100),
                        adresse TEXT
                    )
                """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS commandes (
                        id SERIAL PRIMARY KEY,
                        fournisseur_id INTEGER REFERENCES fournisseurs(id) ON DELETE SET NULL,
                        date_commande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        statut VARCHAR(20),
                        numero_lot VARCHAR(50)
                    )
                """);

            // Add numero_lot column if table already exists without it
            stmt.execute("""
                    DO $$ BEGIN
                        ALTER TABLE commandes ADD COLUMN IF NOT EXISTS numero_lot VARCHAR(50);
                    EXCEPTION WHEN duplicate_column THEN NULL;
                    END $$
                """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS lignes_commande (
                        id SERIAL PRIMARY KEY,
                        commande_id INTEGER REFERENCES commandes(id) ON DELETE CASCADE,
                        medicament_id INTEGER REFERENCES medicaments(id) ON DELETE CASCADE,
                        quantite INTEGER,
                        prix_unitaire DECIMAL(10,2)
                    )
                """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS ventes (
                        id SERIAL PRIMARY KEY,
                        date_vente TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        total_vente DECIMAL(10,2),
                        sur_ordonnance BOOLEAN DEFAULT FALSE
                    )
                """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS lignes_vente (
                        id SERIAL PRIMARY KEY,
                        vente_id INTEGER REFERENCES ventes(id) ON DELETE CASCADE,
                        lot_id INTEGER REFERENCES lots(id) ON DELETE CASCADE,
                        quantite INTEGER,
                        prix_unitaire DECIMAL(10,2)
                    )
                """);

            stmt.execute("""
                    CREATE TABLE IF NOT EXISTS utilisateurs (
                        id SERIAL PRIMARY KEY,
                        username VARCHAR(50) UNIQUE NOT NULL,
                        password_hash VARCHAR(255) NOT NULL,
                        nom VARCHAR(100),
                        prenom VARCHAR(100),
                        role VARCHAR(20) DEFAULT 'USER',
                        date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """);

            // Create indexes on foreign keys (PostgreSQL doesn't auto-index FK)
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_lots_medicament_id ON lots(medicament_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_lignes_commande_commande_id ON lignes_commande(commande_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_lignes_commande_medicament_id ON lignes_commande(medicament_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_commandes_fournisseur_id ON commandes(fournisseur_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_lignes_vente_vente_id ON lignes_vente(vente_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_lignes_vente_lot_id ON lignes_vente(lot_id)");

            createDefaultAdminIfNeeded(conn);
        }
    }

    private static void createDefaultAdminIfNeeded(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) as count FROM utilisateurs WHERE role = 'ADMIN'";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             ResultSet rs = checkStmt.executeQuery()) {

            if (rs.next() && rs.getInt("count") == 0) {
                String adminPasswordHash = "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918";

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
