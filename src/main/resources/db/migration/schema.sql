-- SGPA PostgreSQL Schema
-- Base de données pour le Système de Gestion de Pharmacie Avancé

-- Table Médicaments
CREATE TABLE IF NOT EXISTS medicaments (
    id SERIAL PRIMARY KEY,
    nom_commercial VARCHAR(100) NOT NULL,
    principe_actif VARCHAR(100),
    forme_galenique VARCHAR(50),
    dosage VARCHAR(50),
    prix_public DECIMAL(10,2),
    necessite_ordonnance BOOLEAN DEFAULT FALSE,
    seuil_min_alerte INTEGER
);

-- Table Lots (pour gestion stock avec péremption)
CREATE TABLE IF NOT EXISTS lots (
    id SERIAL PRIMARY KEY,
    medicament_id INTEGER REFERENCES medicaments(id) ON DELETE CASCADE,
    numero_lot VARCHAR(50),
    quantite_stock INTEGER,
    date_peremption DATE,
    prix_achat DECIMAL(10,2)
);

-- Table Fournisseurs
CREATE TABLE IF NOT EXISTS fournisseurs (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    contact VARCHAR(100),
    adresse TEXT
);

-- Table Commandes
CREATE TABLE IF NOT EXISTS commandes (
    id SERIAL PRIMARY KEY,
    fournisseur_id INTEGER REFERENCES fournisseurs(id) ON DELETE SET NULL,
    date_commande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(20),
    numero_lot VARCHAR(50)
);

-- Table Lignes de Commande
CREATE TABLE IF NOT EXISTS lignes_commande (
    id SERIAL PRIMARY KEY,
    commande_id INTEGER REFERENCES commandes(id) ON DELETE CASCADE,
    medicament_id INTEGER REFERENCES medicaments(id) ON DELETE CASCADE,
    quantite INTEGER,
    prix_unitaire DECIMAL(10,2)
);

-- Table Ventes
CREATE TABLE IF NOT EXISTS ventes (
    id SERIAL PRIMARY KEY,
    date_vente TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_vente DECIMAL(10,2),
    sur_ordonnance BOOLEAN DEFAULT FALSE
);

-- Table Lignes de Vente
CREATE TABLE IF NOT EXISTS lignes_vente (
    id SERIAL PRIMARY KEY,
    vente_id INTEGER REFERENCES ventes(id) ON DELETE CASCADE,
    lot_id INTEGER REFERENCES lots(id) ON DELETE CASCADE,
    quantite INTEGER,
    prix_unitaire DECIMAL(10,2)
);

-- Table Utilisateurs
CREATE TABLE IF NOT EXISTS utilisateurs (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_lots_medicament ON lots(medicament_id);
CREATE INDEX IF NOT EXISTS idx_lots_peremption ON lots(date_peremption);
CREATE INDEX IF NOT EXISTS idx_commandes_fournisseur ON commandes(fournisseur_id);
CREATE INDEX IF NOT EXISTS idx_utilisateurs_username ON utilisateurs(username);
