package com.sgpa.service;

import com.sgpa.dao.LotDAO;
import com.sgpa.dao.impl.LotDAOImpl;
import com.sgpa.model.Lot;

import java.util.List;

public class StockService {
    private LotDAO lotDAO;

    public StockService() {
        this.lotDAO = new LotDAOImpl();
    }

    public StockService(LotDAO lotDAO) {
        this.lotDAO = lotDAO;
    }

    public List<Lot> getAllLots() {
        return lotDAO.findAll();
    }
}
