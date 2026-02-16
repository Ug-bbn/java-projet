# SGPA - Système de Gestion de Pharmacie Avancé

Application JavaFX de gestion de pharmacie avec PostgreSQL.

## 🚀 Démarrage Rapide

### Prérequis
- Java JDK 17+
- Maven
- PostgreSQL

### Installation

1. **Créer la base de données PostgreSQL**
```sql
CREATE DATABASE sgpa_db;
```

2. **Configurer la connexion** (si nécessaire)
Modifier `src/main/resources/database.properties` :
```properties
db.url=jdbc:postgresql://localhost:5432/sgpa_db
db.username=postgres
db.password=votre_mot_de_passe
```

3. **Compiler le projet**
```bash
mvn clean compile
```

4. **Charger les données de test** (optionnel)
```bash
.\run-dataloader.ps1
```

5. **Lancer l'application**
```bash
mvn javafx:run
```

### Premier Login
- **Username :** `admin`
- **Password :** `admin`

## 📋 Fonctionnalités

- ✅ Authentification avec gestion des rôles (ADMIN/USER)
- ✅ Gestion des médicaments (CRUD)
- ✅ Gestion du stock par lots avec dates de péremption
- ✅ Gestion des commandes fournisseurs
- ✅ Gestion des ventes
- ✅ Alertes stock faible et péremption
- ✅ Gestion des utilisateurs (admin uniquement)
- ✅ Interface moderne avec thème clair/sombre

## 🛠️ Technologies

- **Java 17** + **JavaFX 21**
- **PostgreSQL** (JDBC 42.7.1)
- **Maven**

## 📁 Structure

```
src/main/
├── java/com/sgpa/
│   ├── controller/     # Contrôleurs JavaFX
│   ├── dao/            # Accès aux données
│   ├── model/          # Modèles
│   ├── service/        # Logique métier
│   └── util/           # Utilitaires
└── resources/
    ├── com/sgpa/       # Vues FXML + CSS
    ├── database.properties
    └── schema-postgresql.sql
```

## 📚 Documentation

Voir le fichier de documentation complet dans le dossier `docs/` ou les artifacts du projet.

## 🐛 Dépannage

**Erreur de connexion PostgreSQL :**
- Vérifier que PostgreSQL est démarré
- Vérifier les identifiants dans `database.properties`

**Tables vides :**
```bash
.\run-dataloader.ps1
```

---

**Projet académique - Dauphine M1 - 2026**
