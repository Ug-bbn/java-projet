package com.sgpa.dao.impl;

import com.sgpa.dao.DAOException;
import com.sgpa.dao.FournisseurDAO;
import com.sgpa.model.Fournisseur;
import com.sgpa.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FournisseurDAOImpl implements FournisseurDAO {
    private static final Logger logger = LoggerFactory.getLogger(FournisseurDAOImpl.class);

    @Override
    public void create(Fournisseur f) {
        String sql = "INSERT INTO fournisseurs (nom, contact, adresse) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, f.getNom());
            stmt.setString(2, f.getContact());
            stmt.setString(3, f.getAdresse());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                f.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la creation du fournisseur", e);
            throw new DAOException("Erreur lors de la creation du fournisseur", e);
        }
    }

    @Override
    public Fournisseur findById(int id) {
        String sql = "SELECT * FROM fournisseurs WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractFournisseur(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche du fournisseur {}", id, e);
            throw new DAOException("Erreur lors de la recherche du fournisseur " + id, e);
        }
        return null;
    }

    @Override
    public List<Fournisseur> findAll() {
        List<Fournisseur> liste = new ArrayList<>();
        String sql = "SELECT * FROM fournisseurs";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(extractFournisseur(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recuperation des fournisseurs", e);
            throw new DAOException("Erreur lors de la recuperation des fournisseurs", e);
        }
        return liste;
    }

    @Override
    public void update(Fournisseur f) {
        String sql = "UPDATE fournisseurs SET nom=?, contact=?, adresse=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, f.getNom());
            stmt.setString(2, f.getContact());
            stmt.setString(3, f.getAdresse());
            stmt.setInt(4, f.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise a jour du fournisseur {}", f.getId(), e);
            throw new DAOException("Erreur lors de la mise a jour du fournisseur " + f.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM fournisseurs WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression du fournisseur {}", id, e);
            throw new DAOException("Erreur lors de la suppression du fournisseur " + id, e);
        }
    }

    private Fournisseur extractFournisseur(ResultSet rs) throws SQLException {
        Fournisseur f = new Fournisseur();
        f.setId(rs.getInt("id"));
        f.setNom(rs.getString("nom"));
        f.setContact(rs.getString("contact"));
        f.setAdresse(rs.getString("adresse"));
        return f;
    }
}
