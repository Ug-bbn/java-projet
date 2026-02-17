package com.sgpa.controller;

import com.sgpa.MainApp;
import com.sgpa.model.Role;
import com.sgpa.model.Utilisateur;
import com.sgpa.util.FXUtil;
import com.sgpa.util.LocalUserData;
import com.sgpa.util.SessionManager;
import com.sgpa.util.Theme;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardTemplateController implements Initializable {

    @FXML
    private BorderPane root;

    @FXML
    private StackPane stckTopBar, stckMin, stckClose;

    @FXML
    private VBox vbxMenuNavigation, vbxMenuTabs;

    @FXML
    private Label lblVersion;

    @FXML
    private StackPane contentArea;

    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblVersion.setText("v1.0"); // Or retrieve dynamically

        // Defer stage access until scene is available
        // Platform.runLater(() -> {
        // stage = (Stage) root.getScene().getWindow();
        // FXUtil.movable(stage, stckTopBar);
        // FXUtil.windowActions(stage, stckMin, stckClose);
        // });

        // Load default view (e.g. Home or Commande)
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/com/sgpa/dashboard-view.fxml"));
            contentArea.getChildren().add(view);
            // Also mark the first menu item as selected
            if (!vbxMenuNavigation.getChildren().isEmpty()) {
                vbxMenuNavigation.getChildren().get(0).getStyleClass().add("selected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        setupNavigation();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        FXUtil.movable(stage, stckTopBar);
        FXUtil.windowActions(stage, stckMin, stckClose);
    }

    private void setupNavigation() {
        // Map navigation items to views.
        // Index mapping:
        // 0: Tableau de bord
        // 1: Médicaments
        // 2: Ventes
        // 3: Stock
        // 4: Commandes
        // 5: Alertes
        // 6: Utilisateurs (ADMIN only)

        boolean isAdmin = false;
        Utilisateur user = SessionManager.getInstance().getUtilisateurConnecte();
        if (user != null && Role.ADMIN.equals(user.getRole())) {
            isAdmin = true;
        }

        int index = 0;
        if (vbxMenuNavigation.getChildren().size() > index) {
            Node nav = vbxMenuNavigation.getChildren().get(index++);
            nav.setOnMouseClicked(e -> navigateTo("/com/sgpa/dashboard-view.fxml", nav));
        }

        if (vbxMenuNavigation.getChildren().size() > index) {
            Node nav = vbxMenuNavigation.getChildren().get(index++);
            nav.setOnMouseClicked(e -> navigateTo("/com/sgpa/medicament-view.fxml", nav));
        }

        if (vbxMenuNavigation.getChildren().size() > index) {
            Node nav = vbxMenuNavigation.getChildren().get(index++);
            nav.setOnMouseClicked(e -> navigateTo("/com/sgpa/vente-view.fxml", nav));
        }

        if (vbxMenuNavigation.getChildren().size() > index) {
            Node nav = vbxMenuNavigation.getChildren().get(index++);
            nav.setOnMouseClicked(e -> navigateTo("/com/sgpa/stock-view.fxml", nav));
        }

        if (vbxMenuNavigation.getChildren().size() > index) {
            Node nav = vbxMenuNavigation.getChildren().get(index++);
            nav.setOnMouseClicked(e -> navigateTo("/com/sgpa/commande-view.fxml", nav));
        }

        if (vbxMenuNavigation.getChildren().size() > index) {
            Node nav = vbxMenuNavigation.getChildren().get(index++);
            nav.setOnMouseClicked(e -> navigateTo("/com/sgpa/alerte-view.fxml", nav));
        }

        // Utilisateurs: visible only for ADMIN
        if (vbxMenuNavigation.getChildren().size() > index) {
            Node nav = vbxMenuNavigation.getChildren().get(index++);
            if (isAdmin) {
                nav.setOnMouseClicked(e -> navigateTo("/com/sgpa/utilisateur-view.fxml", nav));
            } else {
                nav.setVisible(false);
                nav.setManaged(false);
            }
        }
    }

    private void navigateTo(String fxmlPath, Node navNode) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            FXUtil.viewSwitch(contentArea, view);

            // Update selected style
            vbxMenuNavigation.getChildren().forEach(n -> n.getStyleClass().remove("selected"));
            navNode.getStyleClass().add("selected");

        } catch (IOException e) {
            e.printStackTrace();
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
        // Toggle theme logic
        boolean isDark = Boolean.parseBoolean(LocalUserData.getProperty("dark_mode").orElse("false"));
        boolean newMode = !isDark;
        LocalUserData.setProperty("dark_mode", String.valueOf(newMode));

        String themeOrdinal = LocalUserData.getProperty("theme").orElse("0");
        Theme theme = Theme.values()[Integer.parseInt(themeOrdinal)];

        Theme.setCurrentTheme(theme, newMode);

        // Reload CSS on scene
        root.getScene().getStylesheets().clear();
        root.getScene().getStylesheets().add(getClass().getResource("/com/sgpa/css/style.css").toExternalForm());
        root.getScene().getStylesheets().addAll(theme.getThemeFile(),
                newMode ? theme.getDarkFile() : theme.getLightFile());
    }
}
