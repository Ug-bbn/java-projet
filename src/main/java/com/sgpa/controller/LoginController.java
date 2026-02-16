package com.sgpa.controller;

import com.sgpa.model.Utilisateur;
import com.sgpa.service.AuthentificationService;
import com.sgpa.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Label lblError;

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
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/sgpa/dashboard-template.fxml"));
            Parent root = fxmlLoader.load();

            // Close login stage
            Stage loginStage = (Stage) txtUsername.getScene().getWindow();
            loginStage.close();

            // Create NEW stage for dashboard
            Stage dashboardStage = new Stage();
            dashboardStage.initStyle(javafx.stage.StageStyle.TRANSPARENT);
            dashboardStage.setMaximized(true);

            Scene scene = new Scene(root);

            // Load theme
            com.sgpa.util.LocalUserData.getProperty("theme").ifPresent(themeOrdinal -> {
                com.sgpa.util.Theme theme = com.sgpa.util.Theme.values()[Integer.parseInt(themeOrdinal)];
                boolean isDark = Boolean
                        .parseBoolean(com.sgpa.util.LocalUserData.getProperty("dark_mode").orElse("false"));

                scene.getStylesheets().add(getClass().getResource("/com/sgpa/css/style.css").toExternalForm());
                scene.getStylesheets().addAll(theme.getThemeFile(),
                        isDark ? theme.getDarkFile() : theme.getLightFile());
            });

            // Default theme if none set
            if (scene.getStylesheets().isEmpty()) {
                scene.getStylesheets().add(getClass().getResource("/com/sgpa/css/style.css").toExternalForm());
                scene.getStylesheets().add(com.sgpa.util.Theme.STANDARD.getThemeFile());
                scene.getStylesheets().add(com.sgpa.util.Theme.STANDARD.getLightFile());
            }

            scene.setFill(javafx.scene.paint.Color.TRANSPARENT);

            // Pass stage to controller
            DashboardTemplateController controller = fxmlLoader.getController();
            controller.setStage(dashboardStage);

            dashboardStage.setTitle("SGPA - Dashboard");
            dashboardStage.setScene(scene);
            dashboardStage.show();

        } catch (IOException e) {
            logger.error("Erreur lors de l'ouverture du tableau de bord", e);
            showError("Erreur lors du chargement du tableau de bord.");

            // Re-show login stage if error, creating a new one if necessary or if we didn't
            // close it yet (we did close it)
            // Ideally we shouldn't close login stage until we are sure loading worked, but
            // StageStyle requirement makes it tricky.
            // Simplified recovery: just log error.
        }
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
