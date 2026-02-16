package com.sgpa.controller;

import com.sgpa.model.Utilisateur;
import com.sgpa.service.AuthentificationService;
import com.sgpa.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private AuthentificationService authService = new AuthentificationService();

    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        Utilisateur utilisateur = authService.login(username, password);

        if (utilisateur != null) {
            SessionManager.getInstance().login(utilisateur);
            logger.info("Connexion reussie pour {}", username);
            openMainApp();
        } else {
            showError("Identifiants incorrects.");
        }
    }

    private void openMainApp() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/sgpa/main-view.fxml"));
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("SGPA - Systeme de Gestion de Pharmacie Avance");
            stage.setScene(scene);
            stage.setMaximized(true);
        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture de l'application principale", e);
        }
    }


    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
