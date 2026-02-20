# SGPA — Système de Gestion de Pharmacie Avancé

Application de bureau JavaFX pour la gestion complète d'une pharmacie, connectée à une base de données PostgreSQL.

---

## Prérequis

| Outil | Version minimale |
|---|---|
| Java JDK | 17 |
| Maven | 3.8 |
| PostgreSQL | 14 |

---

## Installation & Démarrage

**1. Créer la base de données**
```sql
CREATE DATABASE sgpa_db;
```

**2. Configurer la connexion** — `src/main/resources/database.properties`
```properties
db.url=jdbc:postgresql://localhost:5432/sgpa_db
db.username=postgres
db.password=votre_mot_de_passe
```

**3. Compiler**
```bash
mvn clean compile
```

**4. Charger les données de test** *(optionnel)*
```bash
.\run-dataloader.ps1
```

**5. Lancer**
```bash
mvn javafx:run
```

### Identifiants par défaut
| Champ | Valeur |
|---|---|
| Username | `admin` |
| Password | `admin` |

---

## Fonctionnalités

| Module | Description | Accès |
|---|---|---|
| **Tableau de bord** | KPI, graphiques des ventes, alertes stock/péremption | Tous |
| **Médicaments** | CRUD complet (nom, forme, dosage, prix, seuil d'alerte) | Tous |
| **Ventes** | Enregistrement de ventes, panier, historique | Tous |
| **Stock** | Gestion par lots, dates de péremption, archivage | Tous |
| **Commandes** | Création de commandes fournisseurs, réception en stock | Tous |
| **Fournisseurs** | CRUD complet (nom, contact, adresse) | Tous |
| **Utilisateurs** | Gestion des comptes et des rôles (ADMIN/USER) | Admin uniquement |

### Autres fonctionnalités
- Thème clair / sombre (persisté entre les sessions)
- Alertes visuelles : stock faible, médicaments périmés
- Authentification sécurisée avec hachage BCrypt
- Pool de connexions HikariCP

---

## Technologies

| Technologie | Rôle |
|---|---|
| Java 17 | Langage principal |
| JavaFX 21 | Interface graphique |
| AtlantaFX (PrimerLight / PrimerDark) | Thème de base |
| PostgreSQL 14+ | Base de données |
| HikariCP | Pool de connexions JDBC |
| BCrypt | Hachage des mots de passe |
| SLF4J + Logback | Journalisation |
| Maven | Build & dépendances |
| JUnit 5 | Tests unitaires |

---

## Structure du projet

```
src/
├── main/
│   ├── java/com/sgpa/
│   │   ├── controller/
│   │   │   ├── DashboardTemplateController.java   # Navigation, thème, session
│   │   │   ├── DashboardController.java           # Tableau de bord & KPI
│   │   │   ├── MedicamentController.java
│   │   │   ├── VenteController.java
│   │   │   ├── StockController.java
│   │   │   ├── CommandeController.java
│   │   │   ├── FournisseurController.java         # CRUD fournisseurs
│   │   │   └── UtilisateurController.java
│   │   ├── dao/
│   │   │   ├── FournisseurDAO.java
│   │   │   ├── impl/FournisseurDAOImpl.java
│   │   │   └── ...
│   │   ├── model/
│   │   │   ├── Fournisseur.java
│   │   │   ├── Medicament.java
│   │   │   ├── Commande.java
│   │   │   └── ...
│   │   ├── service/
│   │   │   ├── FournisseurService.java            # Service dédié fournisseurs
│   │   │   ├── CommandeService.java
│   │   │   └── ...
│   │   └── util/
│   │       ├── DatabaseConnection.java            # Pool HikariCP
│   │       ├── SessionManager.java
│   │       └── ...
│   └── resources/com/sgpa/
│       ├── css/style.css
│       ├── dashboard-template.fxml                # Gabarit (sidebar + navigation)
│       ├── fournisseur-view.fxml                  # Vue fournisseurs
│       └── ...
└── test/java/com/sgpa/
    ├── model/
    └── service/
```

---

## Base de données — schéma principal

```sql
fournisseurs (id, nom, contact, adresse)
medicaments  (id, nom_commercial, principe_actif, forme_galenique, dosage, prix, ordonnance, seuil_min_stock)
lots         (id, medicament_id, numero_lot, quantite, date_peremption, prix_achat)
commandes    (id, fournisseur_id, date_commande, statut, numero_lot)
lignes_commande (id, commande_id, medicament_id, quantite, prix_unitaire)
ventes       (id, date_vente, total, avec_ordonnance)
lignes_vente (id, vente_id, medicament_id, quantite, prix_unitaire)
utilisateurs (id, username, password_hash, nom, prenom, role, date_creation)
```

Les tables sont créées automatiquement au démarrage si elles n'existent pas.

---

## Dépannage

| Problème | Solution |
|---|---|
| Erreur de connexion PostgreSQL | Vérifier que le service PostgreSQL est démarré et que `database.properties` est correct |
| Tables vides au premier lancement | Exécuter `.\run-dataloader.ps1` |
| Doublons dans la table fournisseurs | `DELETE FROM fournisseurs WHERE id NOT IN (SELECT MIN(id) FROM fournisseurs GROUP BY nom);` |
| Recompiler après modification | `mvn clean compile` avant `mvn javafx:run` |
