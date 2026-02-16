package com.sgpa.controller;

import com.sgpa.service.AuthentificationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

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
            showSuccess("Compte créé avec succès ! Vous pouvez maintenant vous connecter.");
            // Retour au login après 2 secondes
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    javafx.application.Platform.runLater(this::handleRetourLogin);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
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
            e.printStackTrace();
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
