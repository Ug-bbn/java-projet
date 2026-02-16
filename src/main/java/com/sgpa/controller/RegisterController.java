package com.sgpa.controller;

import com.sgpa.service.AuthentificationService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtPasswordConfirm;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private Label lblError;
    @FXML private Label lblSuccess;

    private AuthentificationService authService = new AuthentificationService();

    @FXML
    private void handleRegister() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String passwordConfirm = txtPasswordConfirm.getText();
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();

        // Validation
        if (username.isEmpty() || password.isEmpty() || nom.isEmpty() || prenom.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (username.length() < 3) {
            showError("Le nom d'utilisateur doit contenir au moins 3 caractères.");
            return;
        }

        if (password.length() < 4) {
            showError("Le mot de passe doit contenir au moins 4 caractères.");
            return;
        }

        if (!password.equals(passwordConfirm)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        // Inscription
        boolean success = authService.register(username, password, nom, prenom);

        if (success) {
            showSuccess("Compte cree avec succes ! Vous pouvez maintenant vous connecter.");
            PauseTransition delay = new PauseTransition(Duration.seconds(2));
            delay.setOnFinished(e -> handleRetourLogin());
            delay.play();
        } else {
            showError("Ce nom d'utilisateur existe déjà.");
        }
    }

    @FXML
    private void handleRetourLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgpa/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) txtUsername.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Connexion - SGPA");
            stage.setMaximized(true);
        } catch (IOException e) {
            logger.error("Erreur lors du retour a la page de connexion", e);
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
        lblSuccess.setVisible(false);
    }

    private void showSuccess(String message) {
        lblSuccess.setText(message);
        lblSuccess.setVisible(true);
        lblError.setVisible(false);
    }
}
