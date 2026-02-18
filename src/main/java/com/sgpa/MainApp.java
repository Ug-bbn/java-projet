package com.sgpa;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerLight().getUserAgentStylesheet());
        org.fxmisc.cssfx.CSSFX.start();

        // idéal pour l'affichage
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/com/sgpa/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Connexion - SGPA");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
