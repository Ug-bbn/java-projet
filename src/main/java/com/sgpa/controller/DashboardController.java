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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
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
    private TableView<Vente> tableDernieresVentes;
    @FXML
    private TableColumn<Vente, Integer> colVenteId;
    @FXML
    private TableColumn<Vente, String> colVenteHeure;
    @FXML
    private TableColumn<Vente, String> colVenteMontant;

    @FXML
    private ListView<AlerteItem> listStockEpuise;
    @FXML
    private ListView<AlerteItem> listMedicamentsPerimes;

    private DashboardService dashboardService;

    public DashboardController() {
        this.dashboardService = new DashboardService();
    }

    @FXML
    public void initialize() {
        setupTables();
        setupAlertsList();
        setupChart();
        refreshData();
    }

    private void setupTables() {
        colVenteId
                .setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getId()));

        colVenteHeure.setCellValueFactory(cell -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cell.getValue().getDateVente().format(DateTimeFormatter.ofPattern("dd/MM HH:mm")));
        });

        colVenteMontant.setCellValueFactory(cell -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cell.getValue().getTotalVente().toPlainString() + " €");
        });
    }

    private void setupAlertsList() {
        javafx.util.Callback<ListView<AlerteItem>, javafx.scene.control.ListCell<AlerteItem>> cellFactory =
                lv -> new javafx.scene.control.ListCell<AlerteItem>() {
            @Override
            protected void updateItem(AlerteItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(item.message);
                    if (item.isCritical) {
                        label.setGraphic(new Label("⚠️"));
                        label.getGraphic().setStyle("-fx-text-fill: -color-danger-fg;");
                        label.setStyle("-fx-text-fill: -color-danger-fg;");
                    } else {
                        label.setGraphic(new Label("⚠️"));
                        label.getGraphic().setStyle("-fx-text-fill: -color-warning-fg;");
                        label.setStyle("-fx-text-fill: -color-fg-default;");
                    }
                    setGraphic(label);
                }
            }
        };
        listStockEpuise.setCellFactory(cellFactory);
        listMedicamentsPerimes.setCellFactory(cellFactory);
    }

    private void setupChart() {
        // Fix Y-Axis formatting to remove "k" for small numbers if needed,
        // essentially just letting it bear normal numbers or currency.
        // Assuming the axis is NumberAxis from FXML.
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
        // Update Table
        List<Vente> ventes = dashboardService.getVenteService().getHistoriqueVentes();
        ventes.sort((v1, v2) -> v2.getDateVente().compareTo(v1.getDateVente()));
        tableDernieresVentes.setItems(FXCollections.observableArrayList(ventes.stream().limit(10).toList()));

        // Update Alerts - Stock Épuisé
        listStockEpuise.getItems().clear();
        List<Medicament> alertesStock = dashboardService.getMedicamentService().getMedicamentsEnAlerteStock();
        alertesStock.forEach(m -> {
            int stock = dashboardService.getMedicamentService().getStockTotal(m.getId());
            listStockEpuise.getItems().add(new AlerteItem(
                    m.getNomCommercial() + " - Stock : " + stock + " (seuil : " + m.getSeuilMinAlerte() + ")", false));
        });

        // Update Alerts - Médicaments Périmés
        listMedicamentsPerimes.getItems().clear();
        List<Lot> alertesPeremption = dashboardService.getMedicamentService().getLotsProchesPeremption();
        alertesPeremption.forEach(l -> {
            String nom = (l.getNomMedicament() != null && !l.getNomMedicament().isEmpty())
                    ? l.getNomMedicament()
                    : "Médicament #" + l.getMedicamentId();
            listMedicamentsPerimes.getItems().add(new AlerteItem(
                    nom + " — Lot n° " + l.getNumeroLot(), true));
        });
    }

    // Simple record for Alerts
    private static class AlerteItem {
        String message;
        boolean isCritical;

        public AlerteItem(String message, boolean isCritical) {
            this.message = message;
            this.isCritical = isCritical;
        }
    }
}
