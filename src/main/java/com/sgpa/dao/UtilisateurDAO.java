package com.sgpa.dao;

import com.sgpa.model.Utilisateur;

import java.util.List;

public interface UtilisateurDAO {
    void create(Utilisateur utilisateur);
    Utilisateur findById(int id);
    Utilisateur findByUsername(String username);
    boolean usernameExists(String username);
    List<Utilisateur> findAll();
    void update(Utilisateur utilisateur);
    void delete(int id);
}
