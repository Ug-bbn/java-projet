package com.sgpa.dao.impl;

import com.sgpa.dao.VenteDAO;
import com.sgpa.model.Vente;
import com.sgpa.model.LigneVente;
import com.sgpa.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VenteDAOImpl implements VenteDAO {
    private static final Logger logger = LoggerFactory.getLogger(VenteDAOImpl.class);

    @Override
    public void create(Vente vente) {
        String sql = "INSERT INTO ventes (date_vente, total_vente, sur_ordonnance) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setTimestamp(1, Timestamp.valueOf(vente.getDateVente()));
            stmt.setBigDecimal(2, vente.getTotalVente());
            stmt.setBoolean(3, vente.isSurOrdonnance());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                vente.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la creation de la vente", e);
        }
    }

    @Override
    public Vente findById(int id) {
        String sql = "SELECT v.*, string_agg(DISTINCT m.nom_commercial, ', ') as noms_medicaments " +
                "FROM ventes v " +
                "LEFT JOIN lignes_vente lv ON v.id = lv.vente_id " +
                "LEFT JOIN lots l ON lv.lot_id = l.id " +
                "LEFT JOIN medicaments m ON l.medicament_id = m.id " +
                "WHERE v.id = ? " +
                "GROUP BY v.id";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractVente(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de la vente {}", id, e);
        }
        return null;
    }

    @Override
    public List<Vente> findAll() {
        List<Vente> liste = new ArrayList<>();
        String sql = "SELECT v.*, string_agg(DISTINCT m.nom_commercial, ', ') as noms_medicaments " +
                "FROM ventes v " +
                "LEFT JOIN lignes_vente lv ON v.id = lv.vente_id " +
                "LEFT JOIN lots l ON lv.lot_id = l.id " +
                "LEFT JOIN medicaments m ON l.medicament_id = m.id " +
                "GROUP BY v.id " +
                "ORDER BY v.date_vente DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(extractVente(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recuperation des ventes", e);
        }
        return liste;
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM ventes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de la vente {}", id, e);
        }
    }

    @Override
    public void addLigneVente(int venteId, LigneVente ligne) {
        String sql = "INSERT INTO lignes_vente (vente_id, lot_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, venteId);
            stmt.setInt(2, ligne.getLotId());
            stmt.setInt(3, ligne.getQuantite());
            stmt.setBigDecimal(4, ligne.getPrixUnitaire());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de l'ajout de la ligne de vente", e);
        }
    }

    @Override
    public List<LigneVente> getLignesVente(int venteId) {
        List<LigneVente> lignes = new ArrayList<>();
        String sql = "SELECT * FROM lignes_vente WHERE vente_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, venteId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LigneVente ligne = new LigneVente();
                ligne.setId(rs.getInt("id"));
                ligne.setVenteId(rs.getInt("vente_id"));
                ligne.setLotId(rs.getInt("lot_id"));
                ligne.setQuantite(rs.getInt("quantite"));
                ligne.setPrixUnitaire(rs.getBigDecimal("prix_unitaire"));
                lignes.add(ligne);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recuperation des lignes de vente {}", venteId, e);
        }
        return lignes;
    }

    private Vente extractVente(ResultSet rs) throws SQLException {
        Vente vente = new Vente();
        vente.setId(rs.getInt("id"));

        Timestamp timestamp = rs.getTimestamp("date_vente");
        if (timestamp != null) {
            vente.setDateVente(timestamp.toLocalDateTime());
        }

        vente.setTotalVente(rs.getBigDecimal("total_vente"));
        vente.setSurOrdonnance(rs.getBoolean("sur_ordonnance"));

        try {
            vente.setNomsMedicaments(rs.getString("noms_medicaments"));
        } catch (SQLException e) {
            // Ignore if column not found
        }

        return vente;
    }
}
