package com.sgpa.controller;

import com.sgpa.model.Lot;
import com.sgpa.model.Medicament;
import com.sgpa.model.Vente;
import com.sgpa.service.DashboardService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private TableView<Vente> tableDernieresVentes;
    @FXML
    private TableColumn<Vente, Integer> colVenteId;
    @FXML
    private TableColumn<Vente, String> colVenteHeure;
    @FXML
    private TableColumn<Vente, String> colVenteMontant;

    @FXML
    private TableView<StockEpuiseItem> tableStockEpuise;
    @FXML
    private TableColumn<StockEpuiseItem, String> colStockNom;
    @FXML
    private TableColumn<StockEpuiseItem, String> colStockForme;

    @FXML
    private TableView<MedicamentPerimeItem> tableMedicamentsPerimes;
    @FXML
    private TableColumn<MedicamentPerimeItem, String> colPerimeNom;
    @FXML
    private TableColumn<MedicamentPerimeItem, String> colPerimeLot;
    @FXML
    private TableColumn<MedicamentPerimeItem, String> colPerimeDate;

    private final DashboardService dashboardService = new DashboardService();

    @FXML
    public void initialize() {
        setupTables();
        setupChart();
        tableDernieresVentes.setPlaceholder(new Label("Aucune vente récente"));
        tableStockEpuise.setPlaceholder(new Label("Aucun stock épuisé"));
        tableMedicamentsPerimes.setPlaceholder(new Label("Aucun médicament périmé en stock"));
        refreshData();
    }

    private void setupTables() {
        colVenteId.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getId()));
        colVenteHeure.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getDateVente().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))));
        colVenteMontant.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getTotalVente().toPlainString() + " €"));

        colStockNom.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNom()));
        colStockForme.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getForme()));

        colPerimeNom.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNom()));
        colPerimeLot.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNumeroLot()));
        colPerimeDate.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDatePeremption()));
    }

    private void setupChart() {
        if (chartVentesSemaine.getXAxis() instanceof javafx.scene.chart.CategoryAxis xAxis) {
            xAxis.setTickLabelRotation(-45);
            xAxis.setTickLabelGap(5);
        }
        if (chartVentesSemaine.getYAxis() instanceof javafx.scene.chart.NumberAxis yAxis) {
            yAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
                @Override
                public String toString(Number object) {
                    return String.format("%.0f €", object.doubleValue());
                }

                @Override
                public Number fromString(String string) {
                    return 0;
                }
            });
        }
    }

    public void refreshData() {
        Task<DashboardSnapshot> task = new Task<>() {
            @Override
            protected DashboardSnapshot call() {
                DashboardSnapshot snap = new DashboardSnapshot();
                snap.totalMedicaments = dashboardService.getTotalMedicaments();
                snap.alertesCount = dashboardService.getAlertesStockCount();
                snap.ventesJour = dashboardService.getVentesDuJour();
                snap.commandesEnAttente = dashboardService.getCommandesEnAttente();
                snap.ventes = dashboardService.getHistoriqueVentes();
                snap.medicaments = dashboardService.getAllMedicaments();
                snap.medicamentsEpuises = dashboardService.getMedicamentsStockEpuise();
                snap.lotsPerimes = dashboardService.getLotsPerimes();

                // Pre-compute stock for pie chart (top 5)
                snap.pieData = new ArrayList<>();
                snap.medicaments.stream().limit(5).forEach(m -> {
                    int stock = dashboardService.getStockTotal(m.getId());
                    if (stock > 0) {
                        snap.pieData.add(new PieChart.Data(m.getNomCommercial(), stock));
                    }
                });

                return snap;
            }
        };

        task.setOnSucceeded(e -> {
            DashboardSnapshot snap = task.getValue();
            applySnapshot(snap);
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex != null) {
                ex.printStackTrace();
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void applySnapshot(DashboardSnapshot snap) {
        // KPIs
        lblTotalMedicaments.setText(String.valueOf(snap.totalMedicaments));
        lblAlertesStock.setText(String.valueOf(snap.alertesCount));
        lblVentesDuJour.setText(snap.ventesJour.toPlainString() + " €");
        lblCommandesEnAttente.setText(String.valueOf(snap.commandesEnAttente));

        // Bar Chart
        chartVentesSemaine.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventes");
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            BigDecimal total = snap.ventes.stream()
                    .filter(v -> v.getDateVente().toLocalDate().isEqual(date))
                    .map(Vente::getTotalVente)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            series.getData().add(new XYChart.Data<>(date.format(formatter), total.doubleValue()));
        }
        chartVentesSemaine.getData().add(series);

        // Pie Chart
        chartRepartitionStock.getData().clear();
        chartRepartitionStock.setData(FXCollections.observableArrayList(snap.pieData));

        // Dernieres Ventes
        snap.ventes.sort((v1, v2) -> v2.getDateVente().compareTo(v1.getDateVente()));
        tableDernieresVentes.setItems(FXCollections.observableArrayList(snap.ventes.stream().limit(10).toList()));

        // Stock Epuise
        ObservableList<StockEpuiseItem> stockItems = FXCollections.observableArrayList();
        for (Medicament m : snap.medicamentsEpuises) {
            stockItems.add(new StockEpuiseItem(m.getNomCommercial(), m.getFormeGalenique(), 0));
        }
        tableStockEpuise.setItems(stockItems);

        // Medicaments Perimes
        ObservableList<MedicamentPerimeItem> perimeItems = FXCollections.observableArrayList();
        Set<String> seen = new HashSet<>();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Lot l : snap.lotsPerimes) {
            String nom = (l.getNomMedicament() != null && !l.getNomMedicament().isEmpty())
                    ? l.getNomMedicament()
                    : "Médicament #" + l.getMedicamentId();
            String key = nom + "|" + l.getNumeroLot();
            if (seen.add(key)) {
                perimeItems.add(new MedicamentPerimeItem(nom, l.getNumeroLot(), l.getDatePeremption().format(dateFmt)));
            }
        }
        tableMedicamentsPerimes.setItems(perimeItems);
    }

    // Snapshot object to transfer data from background thread to FX thread
    private static class DashboardSnapshot {
        int totalMedicaments;
        int alertesCount;
        BigDecimal ventesJour;
        long commandesEnAttente;
        List<Vente> ventes;
        List<Medicament> medicaments;
        List<Medicament> medicamentsEpuises;
        List<Lot> lotsPerimes;
        List<PieChart.Data> pieData;
    }

    public static class StockEpuiseItem {
        private final String nom;
        private final String forme;
        private final int quantite;

        public StockEpuiseItem(String nom, String forme, int quantite) {
            this.nom = nom;
            this.forme = forme;
            this.quantite = quantite;
        }

        public String getNom() { return nom; }
        public String getForme() { return forme; }
        public int getQuantite() { return quantite; }
    }

    public static class MedicamentPerimeItem {
        private final String nom;
        private final String numeroLot;
        private final String datePeremption;

        public MedicamentPerimeItem(String nom, String numeroLot, String datePeremption) {
            this.nom = nom;
            this.numeroLot = numeroLot;
            this.datePeremption = datePeremption;
        }

        public String getNom() { return nom; }
        public String getNumeroLot() { return numeroLot; }
        public String getDatePeremption() { return datePeremption; }
    }
}
