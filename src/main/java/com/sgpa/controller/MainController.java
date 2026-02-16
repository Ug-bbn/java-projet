package com.sgpa.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.sgpa.util.SessionManager;
import com.sgpa.model.Role;
import com.sgpa.model.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private BorderPane contentArea;

    @FXML
    private Label lblUtilisateur;

    @FXML
    private Button btnToggleTheme;

    @FXML
    private Button btnUtilisateurs;

    @FXML
    private javafx.scene.control.Separator separatorAdmin;

    private boolean isDarkMode = true; // Mode sombre par défaut

    @FXML
    private void showDashboard() {
        loadView("dashboard-view.fxml");
    }

    @FXML
    private void showMedicaments() {
        loadView("medicament-view.fxml");
    }

    @FXML
    private void showStock() {
        loadView("stock-view.fxml");
    }

    @FXML
    private void showVentes() {
        loadView("vente-view.fxml");
    }

    @FXML
    private void showCommandes() {
        loadView("commande-view.fxml");
    }

    @FXML
    private void showAlertes() {
        loadView("alerte-view.fxml");
    }

    @FXML
    private void showUtilisateurs() {
        loadView("utilisateur-view.fxml");
    }

    @FXML
    private void handleDeconnexion() {
        SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgpa/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Connexion - SGPA");
            stage.setMaximized(true);
        } catch (IOException e) {
            logger.error("Erreur de navigation", e);
        }
    }

    @FXML
    public void initialize() {
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        if (user != null) {
            lblUtilisateur.setText("👤 " + user.getNomComplet());

            // Afficher le menu Utilisateurs seulement pour les admins
            if (Role.ADMIN.equals(user.getRole())) {
                btnUtilisateurs.setVisible(true);
                btnUtilisateurs.setManaged(true);
                separatorAdmin.setVisible(true);
                separatorAdmin.setManaged(true);
            }
        }

        // Charger le tableau de bord par défaut
        showDashboard();
    }

    @FXML
    private void toggleTheme() {
        isDarkMode = !isDarkMode;

        // Changer le CSS au niveau du root (BorderPane)
        String cssFile = isDarkMode ? "/com/sgpa/style-dark.css" : "/com/sgpa/style-light.css";

        // Obtenir le root BorderPane
        BorderPane root = (BorderPane) contentArea.getParent();
        root.getStylesheets().clear();
        root.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());

        // Mettre à jour le texte du bouton
        if (isDarkMode) {
            btnToggleTheme.setText("☀️ Mode Clair");
        } else {
            btnToggleTheme.setText("🌙 Mode Sombre");
        }
    }

    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgpa/" + fxmlFile));
            javafx.scene.Node view = loader.load();
            contentArea.setCenter(view);
        } catch (IOException e) {
            logger.error("Erreur de navigation", e);
        }
    }
}
