package com.sgpa.dao;

import com.sgpa.model.Fournisseur;
import java.util.List;

public interface FournisseurDAO {
    void create(Fournisseur fournisseur);
    Fournisseur findById(int id);
    List<Fournisseur> findAll();
    void update(Fournisseur fournisseur);
    void delete(int id);
}
