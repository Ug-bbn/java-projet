package com.sgpa.util;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.concurrent.atomic.AtomicReference;

public class FXUtil {

    public static void movable(Stage stage, Pane pane) {
        AtomicReference<Double> xOffset = new AtomicReference<>(0D);
        AtomicReference<Double> yOffset = new AtomicReference<>(0D);

        pane.setOnMousePressed(e -> {
            xOffset.set(e.getSceneX());
            yOffset.set(e.getSceneY());
        });

        pane.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() - xOffset.get());
            stage.setY(e.getScreenY() - yOffset.get());
        });
    }

    public static void windowActions(Stage stage, Node min, Node close) {
        min.setOnMouseClicked(e -> stage.setIconified(true));
        close.setOnMouseClicked(e -> Platform.exit());
    }

    public static void viewSwitch(Pane contentArea, Node newView) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(newView);
    }

    public static void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
