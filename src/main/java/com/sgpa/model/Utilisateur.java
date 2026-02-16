package com.sgpa.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Utilisateur {
    private int id;
    private String username;
    private String passwordHash;
    private String nom;
    private String prenom;
    private Role role;
    private LocalDateTime dateCreation;

    public Utilisateur() {
        this.dateCreation = LocalDateTime.now();
        this.role = Role.USER;
    }

    public Utilisateur(String username, String passwordHash, String nom, String prenom) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
        this.nom = nom;
        this.prenom = prenom;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utilisateur that = (Utilisateur) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Utilisateur{id=%d, username='%s', nom='%s %s', role='%s'}",
            id, username, prenom, nom, role);
    }
}
