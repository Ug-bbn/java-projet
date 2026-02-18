package com.sgpa.util;

import com.sgpa.MainApp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

public class LocalUserData {

    private static final String USER_HOME = System.getProperty("user.home");

    // Using a folder name specific to our app to avoid conflicts
    private static final File FOLDER = new File(USER_HOME + "/.sgpa"),
            PROPERTIES_FILE = new File(FOLDER, "data.properties");

    private static Properties properties = null;

    public static boolean existsFolder() {
        return FOLDER.exists();
    }

    public static Optional<String> getProperty(String key) {
        initProperties();

        if (!properties.containsKey(key)) {
            return Optional.empty();
        }

        return Optional.of(properties.getProperty(key));
    }

    public static void setProperty(String key, String value) {
        initProperties();

        properties.setProperty(key, value);

        try {
            properties.store(new FileOutputStream(PROPERTIES_FILE), "Do not change anything here!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initProperties() {
        if (properties != null)
            return;

        properties = new Properties();

        if (!existsFolder()) {
            FOLDER.mkdir();
        }

        if (!PROPERTIES_FILE.exists()) {
            try {
                properties.store(new FileOutputStream(PROPERTIES_FILE), "Do not change anything here!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            properties.load(new FileInputStream(PROPERTIES_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
