package com.sgpa.service;

import com.sgpa.dao.CommandeDAO;
import com.sgpa.dao.FournisseurDAO;
import com.sgpa.dao.LotDAO;
import com.sgpa.dao.MedicamentDAO;
import com.sgpa.dao.UtilisateurDAO;
import com.sgpa.dao.VenteDAO;
import com.sgpa.dao.impl.CommandeDAOImpl;
import com.sgpa.dao.impl.FournisseurDAOImpl;
import com.sgpa.dao.impl.LotDAOImpl;
import com.sgpa.dao.impl.MedicamentDAOImpl;
import com.sgpa.dao.impl.UtilisateurDAOImpl;
import com.sgpa.dao.impl.VenteDAOImpl;

public class ServiceLocator {
    private static ServiceLocator instance;

    // DAOs
    private MedicamentDAO medicamentDAO;
    private LotDAO lotDAO;
    private FournisseurDAO fournisseurDAO;
    private CommandeDAO commandeDAO;
    private VenteDAO venteDAO;
    private UtilisateurDAO utilisateurDAO;

    // Services
    private MedicamentService medicamentService;
    private StockService stockService;
    private CommandeService commandeService;
    private VenteService venteService;
    private DashboardService dashboardService;
    private AuthentificationService authentificationService;

    private ServiceLocator() {
        initializeDAOs();
        initializeServices();
    }

    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }

    private void initializeDAOs() {
        this.medicamentDAO = new MedicamentDAOImpl();
        this.lotDAO = new LotDAOImpl();
        this.fournisseurDAO = new FournisseurDAOImpl();
        this.commandeDAO = new CommandeDAOImpl();
        this.venteDAO = new VenteDAOImpl();
        this.utilisateurDAO = new UtilisateurDAOImpl();
    }

    private void initializeServices() {
        this.medicamentService = new MedicamentService(medicamentDAO, lotDAO);
        this.stockService = new StockService(lotDAO);
        this.commandeService = new CommandeService(commandeDAO, lotDAO, fournisseurDAO);
        this.venteService = new VenteService(venteDAO, lotDAO, medicamentDAO);
        // DashboardService uses other services
        this.dashboardService = new DashboardService(medicamentService, venteService, commandeService);
        this.authentificationService = new AuthentificationService(utilisateurDAO);
    }

    // Getters for Services
    public MedicamentService getMedicamentService() {
        return medicamentService;
    }

    public StockService getStockService() {
        return stockService;
    }

    public CommandeService getCommandeService() {
        return commandeService;
    }

    public VenteService getVenteService() {
        return venteService;
    }

    public DashboardService getDashboardService() {
        return dashboardService;
    }

    public AuthentificationService getAuthentificationService() {
        return authentificationService;
    }
}
