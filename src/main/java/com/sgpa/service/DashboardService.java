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

    public int getTotalMedicaments() {
        return medicamentService.getAllMedicaments().size();
    }

    public int getAlertesStockCount() {
        return medicamentService.getMedicamentsEnAlerteStock().size();
    }

    public BigDecimal getVentesDuJour() {
        List<Vente> ventes = venteService.getHistoriqueVentes();
        LocalDate today = LocalDate.now();
        return ventes.stream()
                .filter(v -> v.getDateVente().toLocalDate().isEqual(today))
                .map(Vente::getTotalVente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getCommandesEnAttente() {
        List<Commande> commandes = commandeService.getAllCommandes();
        return commandes.stream()
                .filter(c -> c.getStatut() == null || !StatutCommande.RECUE.equals(c.getStatut()))
                .count();
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
