package com.sgpa.controller;

import com.sgpa.model.Lot;
import com.sgpa.model.Medicament;
import com.sgpa.model.Vente;
import com.sgpa.service.DashboardService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class DashboardController {

    @FXML private Label lblTotalMedicaments;
    @FXML private Label lblAlertesStock;
    @FXML private Label lblVentesDuJour;
    @FXML private Label lblCommandesEnAttente;

    @FXML private BarChart<String, Number> chartVentesSemaine;
    @FXML private PieChart chartRepartitionStock;
    @FXML private ListView<String> listDernieresVentes;
    @FXML private ListView<String> listAlertes;

    private DashboardService dashboardService;

    public DashboardController() {
        this.dashboardService = new DashboardService();
    }

    @FXML
    public void initialize() {
        refreshData();
    }

    public void refreshData() {
        updateKPIs();
        updateCharts();
        updateLists();
    }

    private void updateKPIs() {
        lblTotalMedicaments.setText(String.valueOf(dashboardService.getTotalMedicaments()));
        lblAlertesStock.setText(String.valueOf(dashboardService.getAlertesStockCount()));

        BigDecimal ventesJour = dashboardService.getVentesDuJour();
        lblVentesDuJour.setText(ventesJour.toPlainString() + " EUR");

        lblCommandesEnAttente.setText(String.valueOf(dashboardService.getCommandesEnAttente()));
    }

    private void updateCharts() {
        chartVentesSemaine.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventes (EUR)");

        List<Vente> ventes = dashboardService.getVenteService().getHistoriqueVentes();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            BigDecimal total = ventes.stream()
                    .filter(v -> v.getDateVente().toLocalDate().isEqual(date))
                    .map(Vente::getTotalVente)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            series.getData().add(new XYChart.Data<>(date.format(formatter), total.doubleValue()));
        }
        chartVentesSemaine.getData().add(series);

        chartRepartitionStock.getData().clear();
        List<Medicament> meds = dashboardService.getMedicamentService().getAllMedicaments();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        meds.stream().limit(5).forEach(m -> {
            int stock = dashboardService.getMedicamentService().getStockTotal(m.getId());
            if (stock > 0) {
                pieData.add(new PieChart.Data(m.getNomCommercial(), stock));
            }
        });
        chartRepartitionStock.setData(pieData);
    }

    private void updateLists() {
        listDernieresVentes.getItems().clear();
        List<Vente> ventes = dashboardService.getVenteService().getHistoriqueVentes();
        ventes.sort((v1, v2) -> v2.getDateVente().compareTo(v1.getDateVente()));

        ventes.stream().limit(10).forEach(v -> {
            String label = String.format("Vente #%d - %s EUR - %s",
                    v.getId(), v.getTotalVente().toPlainString(),
                    v.getDateVente().format(DateTimeFormatter.ofPattern("HH:mm")));
            listDernieresVentes.getItems().add(label);
        });

        listAlertes.getItems().clear();
        List<Medicament> alertesStock = dashboardService.getMedicamentService().getMedicamentsEnAlerteStock();
        alertesStock.forEach(m -> listAlertes.getItems().add("Stock faible: " + m.getNomCommercial()));

        List<Lot> alertesPeremption = dashboardService.getMedicamentService().getLotsProchesPeremption();
        alertesPeremption.forEach(l -> {
            Medicament m = dashboardService.getMedicamentService().getMedicamentById(l.getMedicamentId());
            String nom = (m != null) ? m.getNomCommercial() : "Lot " + l.getNumeroLot();
            listAlertes.getItems().add("Peremption proche: " + nom + " (" + l.getDatePeremption() + ")");
        });
    }
}
