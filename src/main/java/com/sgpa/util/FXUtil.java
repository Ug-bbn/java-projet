package com.sgpa.util;

import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static javafx.scene.input.MouseEvent.*;

public class FXUtil {
    private static double winWidth = 1240, winHeight = 704;

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

    public static void resizable(Stage stage) {
        ResizeListener resizeListener = new ResizeListener(stage, winWidth, winHeight);
        Scene scene = stage.getScene();

        EventType<?>[] mouseEvents = new EventType<?>[] { MOUSE_MOVED, MOUSE_PRESSED, MOUSE_DRAGGED, MOUSE_EXITED,
                MOUSE_EXITED_TARGET };
        Arrays.stream(mouseEvents).forEach(type -> {
            @SuppressWarnings("unchecked")
            EventType<MouseEvent> mouseEventType = (EventType<MouseEvent>) type;
            scene.addEventHandler(mouseEventType, resizeListener);
        });
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
