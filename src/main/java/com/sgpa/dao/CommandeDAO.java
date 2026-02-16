package com.sgpa.dao;

import com.sgpa.model.Commande;
import com.sgpa.model.LigneCommande;
import java.util.List;

public interface CommandeDAO {
    void create(Commande commande);
    Commande findById(int id);
    List<Commande> findAll();
    void update(Commande commande);
    void delete(int id);
    void addLigneCommande(int commandeId, LigneCommande ligne);
    List<LigneCommande> getLignesCommande(int commandeId);
}
