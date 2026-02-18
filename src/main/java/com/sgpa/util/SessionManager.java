package com.sgpa.util;

import com.sgpa.model.Utilisateur;

public class SessionManager {
    private static SessionManager instance;
    private Utilisateur utilisateurConnecte;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void login(Utilisateur utilisateur) {
        this.utilisateurConnecte = utilisateur;
    }

    public void logout() {
        this.utilisateurConnecte = null;
    }

    public Utilisateur getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    public boolean isLoggedIn() {
        return utilisateurConnecte != null;
    }
}
