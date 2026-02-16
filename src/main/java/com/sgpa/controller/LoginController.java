package com.sgpa.controller;

import com.sgpa.model.Utilisateur;
import com.sgpa.service.AuthentificationService;
import com.sgpa.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private AuthentificationService authService = new AuthentificationService();

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        System.out.println("🔐 Tentative de connexion pour: " + username);

        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        Utilisateur utilisateur = authService.login(username, password);

        if (utilisateur != null) {
            // Connexion réussie
            System.out.println("✅ Authentification réussie pour: " + utilisateur.getNomComplet());
            SessionManager.getInstance().login(utilisateur);
            System.out.println("✅ Session créée");
            openMainApp();
        } else {
            System.out.println("❌ Authentification échouée");
            showError("Identifiants incorrects.");
        }
    }



    private void openMainApp() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/sgpa/main-view.fxml"));
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            
            // Obtenir les dimensions de l'écran
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
            
            // Créer la scène avec les dimensions de l'écran
            Scene scene = new Scene(fxmlLoader.load(), bounds.getWidth(), bounds.getHeight());
            
            stage.setTitle("SGPA - Système de Gestion de Pharmacie Avancé");
            stage.setScene(scene);
            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());
            stage.setMaximized(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
