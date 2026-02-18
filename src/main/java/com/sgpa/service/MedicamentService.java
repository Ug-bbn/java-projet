package com.sgpa.service;

import com.sgpa.dao.MedicamentDAO;
import com.sgpa.dao.LotDAO;
import com.sgpa.dao.impl.MedicamentDAOImpl;
import com.sgpa.dao.impl.LotDAOImpl;
import com.sgpa.model.Medicament;
import com.sgpa.model.Lot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicamentService {
    private static final Logger logger = LoggerFactory.getLogger(MedicamentService.class);
    private MedicamentDAO medicamentDAO;
    private LotDAO lotDAO;

    public MedicamentService() {
        this.medicamentDAO = new MedicamentDAOImpl();
        this.lotDAO = new LotDAOImpl();
    }

    public MedicamentService(MedicamentDAO medicamentDAO, LotDAO lotDAO) {
        this.medicamentDAO = medicamentDAO;
        this.lotDAO = lotDAO;
    }

    public void ajouterMedicament(Medicament medicament) {
        validateMedicament(medicament);
        medicamentDAO.create(medicament);
        logger.info("Medicament {} ajoute", medicament.getNomCommercial());
    }

    public Medicament getMedicamentById(int id) {
        return medicamentDAO.findById(id);
    }

    public List<Medicament> getAllMedicaments() {
        return medicamentDAO.findAll();
    }

    public void modifierMedicament(Medicament medicament) {
        validateMedicament(medicament);
        medicamentDAO.update(medicament);
        logger.info("Medicament {} modifie", medicament.getNomCommercial());
    }

    public void supprimerMedicament(int id) {
        medicamentDAO.delete(id);
        logger.info("Medicament {} supprime", id);
    }

    public List<Medicament> getMedicamentsEnAlerteStock() {
        List<Medicament> allMeds = medicamentDAO.findAll();
        List<Medicament> alertes = new ArrayList<>();

        for (Medicament m : allMeds) {
            int stockTotal = lotDAO.getStockTotal(m.getId());
            if (stockTotal <= m.getSeuilMinAlerte()) {
                alertes.add(m);
            }
        }
        return alertes;
    }

    public List<Lot> getLotsProchesPeremption() {
        LocalDate dateLimite = LocalDate.now().plusMonths(3);
        return lotDAO.findExpiringBefore(dateLimite);
    }

    public List<Lot> getLotsPerimes() {
        return lotDAO.findExpiringBefore(LocalDate.now());
    }

    public int getStockTotal(int medicamentId) {
        return lotDAO.getStockTotal(medicamentId);
    }

    public void validateMedicament(Medicament medicament) {
        if (medicament.getNomCommercial() == null || medicament.getNomCommercial().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom commercial ne peut pas etre vide");
        }
        if (medicament.getPrixPublic() != null && medicament.getPrixPublic().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le prix public ne peut pas etre negatif");
        }
        if (medicament.getSeuilMinAlerte() < 0) {
            throw new IllegalArgumentException("Le seuil minimum d'alerte ne peut pas etre negatif");
        }
    }
}
