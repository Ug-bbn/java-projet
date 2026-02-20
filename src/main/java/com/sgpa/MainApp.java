package com.sgpa;

import com.sgpa.util.DatabaseConnection;
import com.sgpa.util.LocalUserData;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        boolean isDark = Boolean.parseBoolean(LocalUserData.getProperty("dark_mode").orElse("false"));
        if (isDark) {
            Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerDark().getUserAgentStylesheet());
        } else {
            Application.setUserAgentStylesheet(new atlantafx.base.theme.PrimerLight().getUserAgentStylesheet());
        }
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/com/sgpa/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(getClass().getResource("/com/sgpa/css/style.css").toExternalForm());
        stage.setTitle("Connexion - SGPA");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseConnection.close();
    }

    public static void main(String[] args) {
        launch();
    }
}
