package com.sgpa.dao.impl;

import com.sgpa.dao.LotDAO;
import com.sgpa.model.Lot;
import com.sgpa.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LotDAOImpl implements LotDAO {
    private static final Logger logger = LoggerFactory.getLogger(LotDAOImpl.class);

    @Override
    public void create(Lot lot) {
        String sql = "INSERT INTO lots (medicament_id, numero_lot, quantite_stock, date_peremption, prix_achat) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, lot.getMedicamentId());
            stmt.setString(2, lot.getNumeroLot());
            stmt.setInt(3, lot.getQuantiteStock());
            stmt.setDate(4, java.sql.Date.valueOf(lot.getDatePeremption()));
            stmt.setBigDecimal(5, lot.getPrixAchat());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                lot.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la creation du lot", e);
        }
    }

    @Override
    public Lot findById(int id) {
        String sql = "SELECT l.*, m.nom_commercial FROM lots l " +
                "JOIN medicaments m ON l.medicament_id = m.id " +
                "WHERE l.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractLot(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche du lot {}", id, e);
        }
        return null;
    }

    @Override
    public List<Lot> findByMedicamentId(int medicamentId) {
        List<Lot> liste = new ArrayList<>();
        String sql = "SELECT l.*, m.nom_commercial FROM lots l " +
                "JOIN medicaments m ON l.medicament_id = m.id " +
                "WHERE l.medicament_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medicamentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                liste.add(extractLot(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des lots du medicament {}", medicamentId, e);
        }
        return liste;
    }

    @Override
    public List<Lot> findAll() {
        List<Lot> liste = new ArrayList<>();
        String sql = "SELECT l.*, m.nom_commercial FROM lots l " +
                "JOIN medicaments m ON l.medicament_id = m.id";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(extractLot(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recuperation des lots", e);
        }
        return liste;
    }

    @Override
    public void update(Lot lot) {
        String sql = "UPDATE lots SET medicament_id=?, numero_lot=?, quantite_stock=?, date_peremption=?, prix_achat=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, lot.getMedicamentId());
            stmt.setString(2, lot.getNumeroLot());
            stmt.setInt(3, lot.getQuantiteStock());
            stmt.setDate(4, java.sql.Date.valueOf(lot.getDatePeremption()));
            stmt.setBigDecimal(5, lot.getPrixAchat());
            stmt.setInt(6, lot.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise a jour du lot {}", lot.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM lots WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression du lot {}", id, e);
        }
    }

    @Override
    public int getStockTotal(int medicamentId) {
        String sql = "SELECT SUM(quantite_stock) as total FROM lots WHERE medicament_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medicamentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du calcul du stock total du medicament {}", medicamentId, e);
        }
        return 0;
    }

    @Override
    public List<Lot> findExpiringBefore(LocalDate date) {
        List<Lot> liste = new ArrayList<>();
        String sql = "SELECT l.*, m.nom_commercial FROM lots l " +
                "JOIN medicaments m ON l.medicament_id = m.id " +
                "WHERE l.date_peremption < ? AND l.quantite_stock > 0";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                liste.add(extractLot(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des lots proches de peremption", e);
        }
        return liste;
    }

    @Override
    public List<Lot> findByMedicamentIdOrderByDate(int medicamentId) {
        List<Lot> liste = new ArrayList<>();
        String sql = "SELECT l.*, m.nom_commercial FROM lots l " +
                "JOIN medicaments m ON l.medicament_id = m.id " +
                "WHERE l.medicament_id = ? AND l.quantite_stock > 0 " +
                "ORDER BY l.date_peremption ASC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, medicamentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                liste.add(extractLot(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des lots ordonnes du medicament {}", medicamentId, e);
        }
        return liste;
    }

    private Lot extractLot(ResultSet rs) throws SQLException {
        Lot lot = new Lot();
        lot.setId(rs.getInt("id"));
        lot.setMedicamentId(rs.getInt("medicament_id"));
        lot.setNumeroLot(rs.getString("numero_lot"));
        lot.setQuantiteStock(rs.getInt("quantite_stock"));
        lot.setDatePeremption(rs.getDate("date_peremption").toLocalDate());
        lot.setPrixAchat(rs.getBigDecimal("prix_achat"));

        try {
            lot.setNomMedicament(rs.getString("nom_commercial"));
        } catch (SQLException e) {
            // Ignore si la colonne n'est pas presente
        }

        return lot;
    }
}
