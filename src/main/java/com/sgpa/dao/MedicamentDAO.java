package com.sgpa.dao;

import com.sgpa.model.Medicament;
import java.util.List;

public interface MedicamentDAO {
    void create(Medicament medicament);
    Medicament findById(int id);
    List<Medicament> findAll();
    void update(Medicament medicament);
    void delete(int id);
}
