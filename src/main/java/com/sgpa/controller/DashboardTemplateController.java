package com.sgpa.controller;

import com.sgpa.model.Role;
import com.sgpa.model.Utilisateur;
import com.sgpa.util.FXUtil;
import com.sgpa.util.LocalUserData;
import com.sgpa.util.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardTemplateController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(DashboardTemplateController.class);

    @FXML
    private BorderPane root;

    @FXML
    private StackPane stckTopBar, stckMin, stckClose;

    @FXML
    private VBox vbxMenuNavigation, vbxMenuTabs;

    @FXML
    private Label lblDate;

    @FXML
    private Label lblUtilisateur;

    @FXML
    private StackPane contentArea;

    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Afficher la date du jour
        lblDate.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Afficher l'utilisateur connecté
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        if (user != null) {
            lblUtilisateur.setText(user.getNomComplet());
        }

        // Load default view
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgpa/dashboard-view.fxml"));
            Parent view = loader.load();
            contentArea.getChildren().add(view);
            // Also mark the first menu item as selected
            if (!vbxMenuNavigation.getChildren().isEmpty()) {
                vbxMenuNavigation.getChildren().get(0).getStyleClass().add("selected");
            }
        } catch (IOException e) {
            logger.error("Erreur de navigation", e);
        }

        setupNavigation();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        FXUtil.movable(stage, stckTopBar);
        FXUtil.windowActions(stage, stckMin, stckClose);
    }

    private void setupNavigation() {
        // Index mapping (après suppression d'Alertes):
        // 0: Tableau de bord
        // 1: Médicaments
        // 2: Ventes
        // 3: Stock
        // 4: Commandes
        // 5: Utilisateurs (ADMIN only)

        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        boolean isAdmin = user != null && Role.ADMIN.equals(user.getRole());

        String[] views = {
            "/com/sgpa/dashboard-view.fxml",
            "/com/sgpa/medicament-view.fxml",
            "/com/sgpa/vente-view.fxml",
            "/com/sgpa/stock-view.fxml",
            "/com/sgpa/commande-view.fxml",
            "/com/sgpa/utilisateur-view.fxml"
        };

        for (int i = 0; i < vbxMenuNavigation.getChildren().size() && i < views.length; i++) {
            Node nav = vbxMenuNavigation.getChildren().get(i);
            String viewPath = views[i];

            // Utilisateurs: ADMIN only
            if (i == 5) {
                if (isAdmin) {
                    nav.setOnMouseClicked(e -> navigateTo(viewPath, nav));
                } else {
                    nav.setVisible(false);
                    nav.setManaged(false);
                }
            } else {
                nav.setOnMouseClicked(e -> navigateTo(viewPath, nav));
            }
        }
    }

    private void navigateTo(String fxmlPath, Node navNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            FXUtil.viewSwitch(contentArea, view);

            // Update selected style
            vbxMenuNavigation.getChildren().forEach(n -> n.getStyleClass().remove("selected"));
            navNode.getStyleClass().add("selected");

        } catch (IOException e) {
            logger.error("Erreur de navigation", e);
        }
    }

    @FXML
    private void handleDeconnexion() {
        SessionManager.getInstance().logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sgpa/login-view.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/sgpa/css/style.css").toExternalForm());
            Stage currentStage = (Stage) root.getScene().getWindow();
            currentStage.close();

            Stage loginStage = new Stage();
            loginStage.setTitle("Connexion - SGPA");
            loginStage.setScene(scene);
            loginStage.setMaximized(true);
            loginStage.show();
        } catch (IOException e) {
            logger.error("Erreur de navigation", e);
        }
    }

    @FXML
    private SVGPath svgMax;

    @FXML
    private void handleMaximize() {
        if (stage == null) {
            stage = (Stage) root.getScene().getWindow();
        }
        boolean isMax = stage.isMaximized();
        stage.setMaximized(!isMax);

        // Update Icon
        if (svgMax != null) {
            if (stage.isMaximized()) {
                // Restore Icon (Two overlapping squares)
                svgMax.setContent("M4 8H2v10h10v-2H4V8zm12 2V6h-6v4h6zm2-6h-8v6h8V4z");
            } else {
                // Maximize Icon (One square)
                svgMax.setContent("M4 4h16v16H4V4zm2 2v12h12V6H6z");
            }
        }
    }

    @FXML
    private void handleThemeSwitch() {
        boolean isDark = Boolean.parseBoolean(LocalUserData.getProperty("dark_mode").orElse("false"));
        boolean newMode = !isDark;
        LocalUserData.setProperty("dark_mode", String.valueOf(newMode));

        // Switch AtlantaFX base theme (User Agent Stylesheet)
        if (newMode) {
            Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerLight().getUserAgentStylesheet());
        }

        // Reload app CSS on scene
        root.getScene().getStylesheets().clear();
        root.getScene().getStylesheets().add(getClass().getResource("/com/sgpa/css/style.css").toExternalForm());
    }
}
