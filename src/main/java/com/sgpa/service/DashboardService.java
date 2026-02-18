package com.sgpa.service;

import com.sgpa.model.Commande;
import com.sgpa.model.Medicament;
import com.sgpa.model.StatutCommande;
import com.sgpa.model.Vente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    private MedicamentService medicamentService;
    private VenteService venteService;
    private CommandeService commandeService;

    public DashboardService() {
        this.medicamentService = new MedicamentService();
        this.venteService = new VenteService();
        this.commandeService = new CommandeService();
    }

    public DashboardService(MedicamentService medicamentService, VenteService venteService, CommandeService commandeService) {
        this.medicamentService = medicamentService;
        this.venteService = venteService;
        this.commandeService = commandeService;
    }

    public int getTotalMedicaments() {
        return (int) medicamentService.count();
    }

    public int getAlertesStockCount() {
        return medicamentService.getMedicamentsEnAlerteStock().size();
    }

    public BigDecimal getVentesDuJour() {
        return venteService.getTotalVentesDuJour();
    }

    public long getCommandesEnAttente() {
        return commandeService.countEnAttente();
    }

    public MedicamentService getMedicamentService() {
        return medicamentService;
    }

    public VenteService getVenteService() {
        return venteService;
    }

    public CommandeService getCommandeService() {
        return commandeService;
    }
}
