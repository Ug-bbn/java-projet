package com.sgpa.util;

import com.sgpa.MainApp;
import javafx.application.Platform;

public enum Theme {

    STANDARD, BACKIFY;

    public String getThemeFile() {
        return MainApp.class.getResource("/com/sgpa/css/theme/" + this.name().toLowerCase() + "/theme.css")
                .toExternalForm();
    }

    public String getDarkFile() {
        return MainApp.class.getResource("/com/sgpa/css/theme/" + this.name().toLowerCase() + "/dark.css")
                .toExternalForm();
    }

    public String getLightFile() {
        return MainApp.class.getResource("/com/sgpa/css/theme/" + this.name().toLowerCase() + "/light.css")
                .toExternalForm();
    }

    public String getName() {
        return this.name().substring(0, 1).toUpperCase() + this.name().substring(1).toLowerCase().replace("_", " ");
    }

    public static void setCurrentTheme(Theme theme, boolean dark) {
        // App.getInstance().getScene().getStylesheets().removeIf(s ->
        // s.contains("theme.css") || s.contains("dark.css") ||
        // s.contains("light.css"));
        // App.getInstance().getScene().getStylesheets().addAll(theme.getThemeFile(),
        // dark ? theme.getDarkFile() : theme.getLightFile());
        // Note: Logic to restart stylesheets will be in the controller or main app when
        // we have the instance access
    }

}
