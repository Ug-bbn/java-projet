package com.sgpa.service;

import com.sgpa.dao.VenteDAO;
import com.sgpa.dao.LotDAO;
import com.sgpa.dao.MedicamentDAO;
import com.sgpa.dao.impl.VenteDAOImpl;
import com.sgpa.dao.impl.LotDAOImpl;
import com.sgpa.dao.impl.MedicamentDAOImpl;
import com.sgpa.model.Vente;
import com.sgpa.model.LigneVente;
import com.sgpa.model.Lot;
import com.sgpa.model.Medicament;
import com.sgpa.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class VenteService {
    private static final Logger logger = LoggerFactory.getLogger(VenteService.class);
    private VenteDAO venteDAO;
    private LotDAO lotDAO;
    private MedicamentDAO medicamentDAO;

    public VenteService() {
        this.venteDAO = new VenteDAOImpl();
        this.lotDAO = new LotDAOImpl();
        this.medicamentDAO = new MedicamentDAOImpl();
    }

    public VenteService(VenteDAO venteDAO, LotDAO lotDAO, MedicamentDAO medicamentDAO) {
        this.venteDAO = venteDAO;
        this.lotDAO = lotDAO;
        this.medicamentDAO = medicamentDAO;
    }

    public boolean enregistrerVente(int medicamentId, int quantite, boolean surOrdonnance) {
        if (quantite <= 0) {
            logger.warn("Tentative de vente avec une quantite invalide : {}", quantite);
            return false;
        }

        int stockTotal = lotDAO.getStockTotal(medicamentId);
        if (stockTotal < quantite) {
            logger.warn("Stock insuffisant pour le medicament {}. Stock: {}, Demande: {}", medicamentId, stockTotal, quantite);
            return false;
        }

        Medicament medicament = medicamentDAO.findById(medicamentId);
        if (medicament == null) {
            logger.error("Medicament {} introuvable", medicamentId);
            return false;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            try {
                conn.setAutoCommit(false);

                Vente vente = new Vente(surOrdonnance);
                BigDecimal montantTotal = BigDecimal.ZERO;

                List<Lot> lots = lotDAO.findByMedicamentIdOrderByDate(medicamentId);
                int qteRestante = quantite;

                for (Lot lot : lots) {
                    if (qteRestante == 0) break;

                    int qteAPrendre = Math.min(qteRestante, lot.getQuantiteStock());

                    lot.setQuantiteStock(lot.getQuantiteStock() - qteAPrendre);
                    lotDAO.update(lot);

                    LigneVente ligne = new LigneVente(lot.getId(), qteAPrendre, medicament.getPrixPublic());
                    vente.getLignes().add(ligne);

                    montantTotal = montantTotal.add(ligne.getTotal());
                    qteRestante -= qteAPrendre;
                }

                vente.setTotalVente(montantTotal);
                venteDAO.create(vente);

                for (LigneVente ligne : vente.getLignes()) {
                    venteDAO.addLigneVente(vente.getId(), ligne);
                }

                conn.commit();
                logger.info("Vente enregistree avec succes. Total: {} EUR", montantTotal);
                return true;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Erreur lors de l'enregistrement de la vente, rollback effectue", e);
                return false;
            } finally {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            logger.error("Erreur de connexion lors de l'enregistrement de la vente", e);
            return false;
        }
    }

    public List<Vente> getHistoriqueVentes() {
        return venteDAO.findAll();
    }

    public Vente getVenteAvecDetails(int venteId) {
        Vente vente = venteDAO.findById(venteId);
        if (vente != null) {
            vente.setLignes(venteDAO.getLignesVente(venteId));
        }
        return vente;
    }
}
