package com.sgpa.service;

import com.sgpa.dao.CommandeDAO;
import com.sgpa.dao.LotDAO;
import com.sgpa.dao.FournisseurDAO;
import com.sgpa.dao.impl.CommandeDAOImpl;
import com.sgpa.dao.impl.LotDAOImpl;
import com.sgpa.dao.impl.FournisseurDAOImpl;
import com.sgpa.model.Commande;
import com.sgpa.model.LigneCommande;
import com.sgpa.model.Lot;
import com.sgpa.model.Fournisseur;
import com.sgpa.model.StatutCommande;
import com.sgpa.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CommandeService {
    private static final Logger logger = LoggerFactory.getLogger(CommandeService.class);
    private CommandeDAO commandeDAO;
    private LotDAO lotDAO;
    private FournisseurDAO fournisseurDAO;

    public CommandeService() {
        this.commandeDAO = new CommandeDAOImpl();
        this.lotDAO = new LotDAOImpl();
        this.fournisseurDAO = new FournisseurDAOImpl();
    }

    public void creerCommande(int fournisseurId, List<LigneCommande> lignes) {
        Commande commande = new Commande(fournisseurId);
        commandeDAO.create(commande);

        for (LigneCommande ligne : lignes) {
            commandeDAO.addLigneCommande(commande.getId(), ligne);
        }

        logger.info("Commande {} creee avec succes", commande.getId());
    }

    public void recevoirCommande(int commandeId, String numeroLot, LocalDate datePeremption) {
        Commande commande = commandeDAO.findById(commandeId);
        if (commande == null) {
            logger.warn("Commande {} introuvable", commandeId);
            return;
        }

        if (StatutCommande.RECUE.equals(commande.getStatut())) {
            logger.warn("La commande {} a deja ete recue", commandeId);
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            try {
                conn.setAutoCommit(false);

                List<LigneCommande> lignes = commandeDAO.getLignesCommande(commandeId);

                for (LigneCommande ligne : lignes) {
                    Lot lot = new Lot(
                            ligne.getMedicamentId(),
                            numeroLot + "-" + ligne.getMedicamentId(),
                            ligne.getQuantite(),
                            datePeremption,
                            ligne.getPrixUnitaire());
                    lotDAO.create(lot);
                }

                commande.setStatut(StatutCommande.RECUE);
                commandeDAO.update(commande);

                conn.commit();
                logger.info("Commande {} receptionnee avec succes", commandeId);
            } catch (Exception e) {
                conn.rollback();
                logger.error("Erreur lors de la reception de la commande {}, rollback effectue", commandeId, e);
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            logger.error("Erreur de connexion lors de la reception de la commande {}", commandeId, e);
        }
    }

    public List<Commande> getAllCommandes() {
        return commandeDAO.findAll();
    }

    public List<LigneCommande> getLignesCommande(int commandeId) {
        return commandeDAO.getLignesCommande(commandeId);
    }

    public void ajouterFournisseur(Fournisseur fournisseur) {
        fournisseurDAO.create(fournisseur);
        logger.info("Fournisseur {} ajoute", fournisseur.getNom());
    }

    public List<Fournisseur> getAllFournisseurs() {
        return fournisseurDAO.findAll();
    }
}
