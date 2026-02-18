package com.sgpa.controller;

import com.sgpa.model.Lot;
import com.sgpa.model.Medicament;
import com.sgpa.model.Vente;
import com.sgpa.service.DashboardService;
import com.sgpa.service.ServiceLocator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    // --- New Tables ---

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

    private DashboardService dashboardService;

    public DashboardController() {
        this.dashboardService = ServiceLocator.getInstance().getDashboardService();
    }

    @FXML
    public void initialize() {
        setupTables();
        setupChart();
        refreshData();
    }

    private void setupTables() {
        // Dernieres Ventes
        colVenteId.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getId()));
        colVenteHeure.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getDateVente().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))));
        colVenteMontant.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(
                cell.getValue().getTotalVente().toPlainString() + " €"));

        // Stock Epuise
        colStockNom.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNom()));
        colStockForme.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getForme()));

        // Medicaments Perimes
        colPerimeNom.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNom()));
        colPerimeLot.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getNumeroLot()));
        colPerimeDate.setCellValueFactory(cell -> new javafx.beans.property.SimpleStringProperty(cell.getValue().getDatePeremption()));
    }

    private void setupChart() {
        if (chartVentesSemaine.getYAxis() instanceof javafx.scene.chart.NumberAxis) {
            javafx.scene.chart.NumberAxis yAxis = (javafx.scene.chart.NumberAxis) chartVentesSemaine.getYAxis();
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
        updateKPIs();
        updateCharts();
        updateLists();
    }

    private void updateKPIs() {
        lblTotalMedicaments.setText(String.valueOf(dashboardService.getTotalMedicaments()));
        lblAlertesStock.setText(String.valueOf(dashboardService.getAlertesStockCount()));

        BigDecimal ventesJour = dashboardService.getVentesDuJour();
        lblVentesDuJour.setText(ventesJour.toPlainString() + " €");

        lblCommandesEnAttente.setText(String.valueOf(dashboardService.getCommandesEnAttente()));
    }

    private void updateCharts() {
        chartVentesSemaine.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventes");

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
        // Update Table Dernieres Ventes
        List<Vente> ventes = dashboardService.getVenteService().getHistoriqueVentes();
        ventes.sort((v1, v2) -> v2.getDateVente().compareTo(v1.getDateVente()));
        tableDernieresVentes.setItems(FXCollections.observableArrayList(ventes.stream().limit(10).toList()));

        // Update Alerts - Stock Épuisé
        // Note: Client considers "Épuisé" as strictly 0.
        // We fetch all low stock items but only display those with 0 quantity.
        List<Medicament> alertesStock = dashboardService.getMedicamentService().getMedicamentsEnAlerteStock();
        ObservableList<StockEpuiseItem> stockItems = FXCollections.observableArrayList();

        for (Medicament m : alertesStock) {
            int stock = dashboardService.getMedicamentService().getStockTotal(m.getId());
            if (stock == 0) {
                stockItems.add(new StockEpuiseItem(
                        m.getNomCommercial(),
                        m.getFormeGalenique(),
                        stock
                ));
            }
        }
        tableStockEpuise.setItems(stockItems);

        // Update Alerts - Médicaments Périmés
        List<Lot> allLots = dashboardService.getMedicamentService().getLotsProchesPeremption();
        // getLotsProchesPeremption likely returns lots close to expiry or expired.
        // We strictly want expired: datePeremption <= LocalDate.now()

        ObservableList<MedicamentPerimeItem> perimeItems = FXCollections.observableArrayList();
        Set<String> seen = new HashSet<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Lot l : allLots) {
            if (!l.getDatePeremption().isAfter(today)) {
                String nom = (l.getNomMedicament() != null && !l.getNomMedicament().isEmpty())
                        ? l.getNomMedicament()
                        : "Médicament #" + l.getMedicamentId();

                // Key for duplicates: MedicamentName + BatchNumber
                String key = nom + "|" + l.getNumeroLot();
                if (seen.add(key)) {
                    perimeItems.add(new MedicamentPerimeItem(
                            nom,
                            l.getNumeroLot(),
                            l.getDatePeremption().format(dateFmt)
                    ));
                }
            }
        }
        tableMedicamentsPerimes.setItems(perimeItems);
    }

    // --- Inner Classes for Table Views ---

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
