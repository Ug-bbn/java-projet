package com.sgpa.dao;

import com.sgpa.model.Vente;
import com.sgpa.model.LigneVente;
import java.sql.Connection;
import java.util.List;

public interface VenteDAO {
    void create(Vente vente);
    void create(Vente vente, Connection conn);
    Vente findById(int id);
    List<Vente> findAll();
    void delete(int id);
    void addLigneVente(int venteId, LigneVente ligne);
    void addLigneVente(int venteId, LigneVente ligne, Connection conn);
    List<LigneVente> getLignesVente(int venteId);
    java.math.BigDecimal sumTotalByDate(java.time.LocalDate date);
}
