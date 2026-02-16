package com.sgpa.service;

import com.sgpa.dao.LotDAO;
import com.sgpa.dao.impl.LotDAOImpl;
import com.sgpa.model.Lot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class StockService {
    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    private LotDAO lotDAO;

    public StockService() {
        this.lotDAO = new LotDAOImpl();
    }

    public StockService(LotDAO lotDAO) {
        this.lotDAO = lotDAO;
    }

    public void ajouterLot(Lot lot) {
        validateLot(lot);
        lotDAO.create(lot);
        logger.info("Lot {} ajoute avec succes", lot.getNumeroLot());
    }

    public List<Lot> getAllLots() {
        return lotDAO.findAll();
    }

    public void supprimerLot(int id) {
        lotDAO.delete(id);
        logger.info("Lot {} supprime", id);
    }

    private void validateLot(Lot lot) {
        if (lot.getQuantiteStock() < 0) {
            throw new IllegalArgumentException("La quantite ne peut pas etre negative");
        }
        if (lot.getPrixAchat() != null && lot.getPrixAchat().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Le prix d'achat ne peut pas etre negatif");
        }
        if (lot.getDatePeremption() != null && lot.getDatePeremption().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La date de peremption doit etre dans le futur");
        }
    }
}
