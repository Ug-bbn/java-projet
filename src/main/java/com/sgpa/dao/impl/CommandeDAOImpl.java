package com.sgpa.dao.impl;

import com.sgpa.dao.CommandeDAO;
import com.sgpa.model.Commande;
import com.sgpa.model.LigneCommande;
import com.sgpa.model.StatutCommande;
import com.sgpa.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommandeDAOImpl implements CommandeDAO {
    private static final Logger logger = LoggerFactory.getLogger(CommandeDAOImpl.class);

    @Override
    public void create(Commande cmd) {
        String sql = "INSERT INTO commandes (fournisseur_id, date_commande, statut) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, cmd.getFournisseurId());
            stmt.setTimestamp(2, Timestamp.valueOf(cmd.getDateCommande()));
            stmt.setString(3, cmd.getStatut().getLabel());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                cmd.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la creation de la commande", e);
        }
    }

    @Override
    public Commande findById(int id) {
        String sql = "SELECT * FROM commandes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractCommande(rs);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de la commande {}", id, e);
        }
        return null;
    }

    @Override
    public List<Commande> findAll() {
        List<Commande> liste = new ArrayList<>();
        String sql = "SELECT * FROM commandes";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                liste.add(extractCommande(rs));
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recuperation des commandes", e);
        }
        return liste;
    }

    @Override
    public void update(Commande cmd) {
        String sql = "UPDATE commandes SET fournisseur_id=?, date_commande=?, statut=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cmd.getFournisseurId());
            stmt.setTimestamp(2, Timestamp.valueOf(cmd.getDateCommande()));
            stmt.setString(3, cmd.getStatut().getLabel());
            stmt.setInt(4, cmd.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise a jour de la commande {}", cmd.getId(), e);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM commandes WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de la commande {}", id, e);
        }
    }

    @Override
    public void addLigneCommande(int commandeId, LigneCommande ligne) {
        String sql = "INSERT INTO lignes_commande (commande_id, medicament_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commandeId);
            stmt.setInt(2, ligne.getMedicamentId());
            stmt.setInt(3, ligne.getQuantite());
            stmt.setBigDecimal(4, ligne.getPrixUnitaire());

            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Erreur lors de l'ajout de la ligne de commande", e);
        }
    }

    @Override
    public List<LigneCommande> getLignesCommande(int commandeId) {
        List<LigneCommande> lignes = new ArrayList<>();
        String sql = "SELECT * FROM lignes_commande WHERE commande_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commandeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LigneCommande ligne = new LigneCommande();
                ligne.setId(rs.getInt("id"));
                ligne.setCommandeId(rs.getInt("commande_id"));
                ligne.setMedicamentId(rs.getInt("medicament_id"));
                ligne.setQuantite(rs.getInt("quantite"));
                ligne.setPrixUnitaire(rs.getBigDecimal("prix_unitaire"));
                lignes.add(ligne);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recuperation des lignes de commande {}", commandeId, e);
        }
        return lignes;
    }

    private Commande extractCommande(ResultSet rs) throws SQLException {
        Commande cmd = new Commande();
        cmd.setId(rs.getInt("id"));
        cmd.setFournisseurId(rs.getInt("fournisseur_id"));
        cmd.setDateCommande(rs.getTimestamp("date_commande").toLocalDateTime());
        cmd.setStatut(StatutCommande.fromString(rs.getString("statut")));
        return cmd;
    }
}
