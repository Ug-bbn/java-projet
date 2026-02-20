package com.sgpa.service;

import com.sgpa.dao.FournisseurDAO;
import com.sgpa.dao.impl.FournisseurDAOImpl;
import com.sgpa.model.Fournisseur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class FournisseurService {

    private static final Logger logger = LoggerFactory.getLogger(FournisseurService.class);

    private final FournisseurDAO fournisseurDAO;

    public FournisseurService() {
        this.fournisseurDAO = new FournisseurDAOImpl();
    }

    public void creer(Fournisseur fournisseur) {
        fournisseurDAO.create(fournisseur);
        logger.info("Fournisseur {} cree", fournisseur.getNom());
    }

    public List<Fournisseur> getAll() {
        return fournisseurDAO.findAll();
    }

    public void modifier(Fournisseur fournisseur) {
        fournisseurDAO.update(fournisseur);
        logger.info("Fournisseur {} modifie", fournisseur.getNom());
    }

    public void supprimer(int id) {
        fournisseurDAO.delete(id);
        logger.info("Fournisseur {} supprime", id);
    }
}
