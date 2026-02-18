package com.sgpa.dao.impl;

import com.sgpa.dao.DAOException;
import com.sgpa.dao.MedicamentDAO;
import com.sgpa.model.Medicament;
import com.sgpa.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicamentDAOImpl implements MedicamentDAO {
    private static final Logger logger = LoggerFactory.getLogger(MedicamentDAOImpl.class);

    @Override
    public void create(Medicament m) {
        String sql = "INSERT INTO medicaments (nom_commercial, principe_actif, forme_galenique, dosage, prix_public, necessite_ordonnance, seuil_min_alerte) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, m.getNomCommercial());
            stmt.setString(2, m.getPrincipeActif());
            stmt.setString(3, m.getFormeGalenique());
            stmt.setString(4, m.getDosage());
            stmt.setBigDecimal(5, m.getPrixPublic());
            stmt.setBoolean(6, m.isNecessiteOrdonnance());
            stmt.setInt(7, m.getSeuilMinAlerte());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                m.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la creation du medicament", e);
            throw new DAOException("Erreur lors de la creation du medicament", e);
        }
    }

    @Override
    public Medicament findById(int id) {
        String sql = "SELECT * FROM medicaments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractMedicament(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche du medicament {}", id, e);
            throw new DAOException("Erreur lors de la recherche du medicament " + id, e);
        }
        return null;
    }

    @Override
    public List<Medicament> findAll() {
        List<Medicament> liste = new ArrayList<>();
        String sql = "SELECT * FROM medicaments";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(extractMedicament(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recuperation des medicaments", e);
            throw new DAOException("Erreur lors de la recuperation des medicaments", e);
        }
        return liste;
    }

    @Override
    public void update(Medicament m) {
        String sql = "UPDATE medicaments SET nom_commercial=?, principe_actif=?, forme_galenique=?, dosage=?, prix_public=?, necessite_ordonnance=?, seuil_min_alerte=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, m.getNomCommercial());
            stmt.setString(2, m.getPrincipeActif());
            stmt.setString(3, m.getFormeGalenique());
            stmt.setString(4, m.getDosage());
            stmt.setBigDecimal(5, m.getPrixPublic());
            stmt.setBoolean(6, m.isNecessiteOrdonnance());
            stmt.setInt(7, m.getSeuilMinAlerte());
            stmt.setInt(8, m.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise a jour du medicament {}", m.getId(), e);
            throw new DAOException("Erreur lors de la mise a jour du medicament " + m.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM medicaments WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression du medicament {}", id, e);
            throw new DAOException("Erreur lors de la suppression du medicament " + id, e);
        }
    }

    private Medicament extractMedicament(ResultSet rs) throws SQLException {
        Medicament m = new Medicament();
        m.setId(rs.getInt("id"));
        m.setNomCommercial(rs.getString("nom_commercial"));
        m.setPrincipeActif(rs.getString("principe_actif"));
        m.setFormeGalenique(rs.getString("forme_galenique"));
        m.setDosage(rs.getString("dosage"));
        m.setPrixPublic(rs.getBigDecimal("prix_public"));
        m.setNecessiteOrdonnance(rs.getBoolean("necessite_ordonnance"));
        m.setSeuilMinAlerte(rs.getInt("seuil_min_alerte"));
        return m;
    }
}
