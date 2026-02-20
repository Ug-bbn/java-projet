package com.sgpa.util;

import com.sgpa.model.Utilisateur;

public class SessionManager {
    private static volatile SessionManager instance;
    private volatile Utilisateur utilisateurConnecte;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
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

}
