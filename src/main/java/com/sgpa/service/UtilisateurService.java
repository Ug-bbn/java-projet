package com.sgpa.service;

import com.sgpa.dao.UtilisateurDAO;
import com.sgpa.dao.impl.UtilisateurDAOImpl;
import com.sgpa.model.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UtilisateurService {
    private static final Logger logger = LoggerFactory.getLogger(UtilisateurService.class);
    private UtilisateurDAO utilisateurDAO;
    private AuthentificationService authService;

    public UtilisateurService() {
        this.utilisateurDAO = new UtilisateurDAOImpl();
        this.authService = new AuthentificationService();
    }

    public List<Utilisateur> getAllUtilisateurs() {
        return utilisateurDAO.findAll();
    }

    public boolean createUtilisateur(String username, String password, String nom, String prenom, String role) {
        return authService.register(username, password, nom, prenom, role);
    }

    public void updateUtilisateur(Utilisateur utilisateur, String newPassword) {
        if (newPassword != null && !newPassword.isEmpty()) {
            String hashedPassword = authService.hashPassword(newPassword);
            utilisateur.setPasswordHash(hashedPassword);
        }
        utilisateurDAO.update(utilisateur);
        logger.info("Utilisateur {} mis a jour", utilisateur.getUsername());
    }

    public void deleteUtilisateur(int id) {
        utilisateurDAO.delete(id);
        logger.info("Utilisateur {} supprime", id);
    }

    public boolean usernameExists(String username) {
        return utilisateurDAO.usernameExists(username);
    }
}
