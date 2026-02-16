package com.sgpa.dao.impl;

import com.sgpa.dao.UtilisateurDAO;
import com.sgpa.model.Role;
import com.sgpa.model.Utilisateur;
import com.sgpa.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class UtilisateurDAOImpl implements UtilisateurDAO {
    private static final Logger logger = LoggerFactory.getLogger(UtilisateurDAOImpl.class);

    @Override
    public void create(Utilisateur utilisateur) {
        String sql = "INSERT INTO utilisateurs (username, password_hash, nom, prenom, role, date_creation) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, utilisateur.getUsername());
            pstmt.setString(2, utilisateur.getPasswordHash());
            pstmt.setString(3, utilisateur.getNom());
            pstmt.setString(4, utilisateur.getPrenom());
            pstmt.setString(5, utilisateur.getRole().getLabel());
            pstmt.setTimestamp(6, java.sql.Timestamp.valueOf(utilisateur.getDateCreation()));

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                utilisateur.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la creation de l'utilisateur", e);
        }
    }

    @Override
    public Utilisateur findById(int id) {
        String sql = "SELECT * FROM utilisateurs WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de l'utilisateur {}", id, e);
        }
        return null;
    }

    @Override
    public Utilisateur findByUsername(String username) {
        String sql = "SELECT * FROM utilisateurs WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUtilisateur(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de l'utilisateur {}", username, e);
        }
        return null;
    }

    @Override
    public boolean usernameExists(String username) {
        return findByUsername(username) != null;
    }

    @Override
    public List<Utilisateur> findAll() {
        String sql = "SELECT * FROM utilisateurs ORDER BY username";
        List<Utilisateur> utilisateurs = new java.util.ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                utilisateurs.add(mapResultSetToUtilisateur(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recuperation des utilisateurs", e);
        }
        return utilisateurs;
    }

    @Override
    public void update(Utilisateur utilisateur) {
        String sql = "UPDATE utilisateurs SET username = ?, password_hash = ?, nom = ?, prenom = ?, role = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, utilisateur.getUsername());
            pstmt.setString(2, utilisateur.getPasswordHash());
            pstmt.setString(3, utilisateur.getNom());
            pstmt.setString(4, utilisateur.getPrenom());
            pstmt.setString(5, utilisateur.getRole().getLabel());
            pstmt.setInt(6, utilisateur.getId());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise a jour de l'utilisateur {}", utilisateur.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM utilisateurs WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de l'utilisateur {}", id, e);
        }
    }

    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(rs.getInt("id"));
        utilisateur.setUsername(rs.getString("username"));
        utilisateur.setPasswordHash(rs.getString("password_hash"));
        utilisateur.setNom(rs.getString("nom"));
        utilisateur.setPrenom(rs.getString("prenom"));
        utilisateur.setRole(Role.fromString(rs.getString("role")));

        String dateStr = rs.getString("date_creation");
        if (dateStr != null) {
            dateStr = dateStr.replace(" ", "T");
            utilisateur.setDateCreation(LocalDateTime.parse(dateStr));
        }

        return utilisateur;
    }
}
