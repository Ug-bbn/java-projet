package com.sgpa.dao;

import com.sgpa.model.Lot;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

public interface LotDAO {
    void create(Lot lot);
    void create(Lot lot, Connection conn);
    Lot findById(int id);
    List<Lot> findByMedicamentId(int medicamentId);
    List<Lot> findAll();
    void update(Lot lot);
    void update(Lot lot, Connection conn);
    void delete(int id);
    int getStockTotal(int medicamentId);
    List<Lot> findExpiringBefore(LocalDate date);
    List<Lot> findByMedicamentIdOrderByDate(int medicamentId);
    List<Lot> findByMedicamentIdOrderByDate(int medicamentId, Connection conn);
}
