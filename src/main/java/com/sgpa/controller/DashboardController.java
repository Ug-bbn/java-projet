package com.sgpa.controller;

import com.sgpa.model.Commande;
import com.sgpa.model.Lot;
import com.sgpa.model.Medicament;
import com.sgpa.model.Vente;
import com.sgpa.service.CommandeService;
import com.sgpa.service.MedicamentService;
import com.sgpa.service.VenteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML
    private Label lblTotalMedicaments;
    @FXML
    private Label lblAlertesStock;
    @FXML
    private Label lblVentesDuJour;
    @FXML
    private Label lblCommandesEnAttente;

    @FXML
    private BarChart<String, Number> chartVentesSemaine;
    @FXML
    private PieChart chartRepartitionStock;
    @FXML
    private ListView<String> listDernieresVentes;
    @FXML
    private ListView<String> listAlertes;

    private MedicamentService medicamentService;
    private VenteService venteService;
    private CommandeService commandeService;

    public DashboardController() {
        this.medicamentService = new MedicamentService();
        this.venteService = new VenteService();
        this.commandeService = new CommandeService();
    }

    @FXML
    public void initialize() {
        refreshData();
    }

    public void refreshData() {
        // 1. KPI Cards
        updateKPIs();

        // 2. Charts
        updateCharts();

        // 3. Lists
        updateLists();
    }

    private void updateKPIs() {
        // Total Médicaments
        List<Medicament> meds = medicamentService.getAllMedicaments();
        lblTotalMedicaments.setText(String.valueOf(meds.size()));

        // Alertes Stock
        List<Medicament> alertes = medicamentService.getMedicamentsEnAlerteStock();
        lblAlertesStock.setText(String.valueOf(alertes.size()));

        // Ventes du jour
        List<Vente> ventes = venteService.getHistoriqueVentes();
        LocalDate today = LocalDate.now();
        double totalJour = ventes.stream()
                .filter(v -> v.getDateVente().toLocalDate().isEqual(today))
                .mapToDouble(Vente::getTotalVente)
                .sum();
        lblVentesDuJour.setText(String.format("%.2f €", totalJour));

        // Commandes en attente
        List<Commande> commandes = commandeService.getAllCommandes();
        long enAttente = commandes.stream()
                .filter(c -> c.getStatut() == null || !c.getStatut().equals("RECUE"))
                .count();
        lblCommandesEnAttente.setText(String.valueOf(enAttente));
    }

    private void updateCharts() {
        // BarChart: Ventes des 7 derniers jours
        chartVentesSemaine.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventes (€)");

        List<Vente> ventes = venteService.getHistoriqueVentes();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            double total = ventes.stream()
                    .filter(v -> v.getDateVente().toLocalDate().isEqual(date))
                    .mapToDouble(Vente::getTotalVente)
                    .sum();
            series.getData().add(new XYChart.Data<>(date.format(formatter), total));
        }
        chartVentesSemaine.getData().add(series);

        // PieChart: Top 5 Médicaments par stock (valeur)
        chartRepartitionStock.getData().clear();
        List<Medicament> meds = medicamentService.getAllMedicaments();

        // On prend les 5 premiers pour l'exemple, idéalement on trierait par valeur du
        // stock
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        meds.stream().limit(5).forEach(m -> {
            int stock = medicamentService.getStockTotal(m.getId());
            if (stock > 0) {
                pieData.add(new PieChart.Data(m.getNomCommercial(), stock));
            }
        });
        chartRepartitionStock.setData(pieData);
    }

    private void updateLists() {
        // Dernières ventes
        listDernieresVentes.getItems().clear();
        List<Vente> ventes = venteService.getHistoriqueVentes();
        // Trier par date décroissante (les plus récentes en premier)
        // Note: Idéalement faire ce tri en SQL ou dans le Service
        ventes.sort((v1, v2) -> v2.getDateVente().compareTo(v1.getDateVente()));

        ventes.stream().limit(10).forEach(v -> {
            String label = String.format("Vente #%d - %.2f € - %s",
                    v.getId(), v.getTotalVente(),
                    v.getDateVente().format(DateTimeFormatter.ofPattern("HH:mm")));
            listDernieresVentes.getItems().add(label);
        });

        // Alertes (Stock & Péremption)
        listAlertes.getItems().clear();
        List<Medicament> alertesStock = medicamentService.getMedicamentsEnAlerteStock();
        alertesStock.forEach(m -> listAlertes.getItems().add("⚠️ Stock faible: " + m.getNomCommercial()));

        List<Lot> alertesPeremption = medicamentService.getLotsProchesPeremption();
        alertesPeremption.forEach(l -> {
            Medicament m = medicamentService.getMedicamentById(l.getMedicamentId()); // Optimisation possible
            String nom = (m != null) ? m.getNomCommercial() : "Lot " + l.getNumeroLot();
            listAlertes.getItems().add("⏰ Péremption proche: " + nom + " (" + l.getDatePeremption() + ")");
        });
    }
}
