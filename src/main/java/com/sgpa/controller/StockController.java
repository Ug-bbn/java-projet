package com.sgpa.controller;

import com.sgpa.model.Lot;
import com.sgpa.model.Medicament;
import com.sgpa.service.StockService;
import com.sgpa.service.MedicamentService;
import com.sgpa.service.ServiceLocator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class StockController {

    @FXML
    private TableView<Lot> tableLots;
    @FXML
    private TableColumn<Lot, Integer> colId;
    @FXML
    private TableColumn<Lot, String> colMedicament;
    @FXML
    private TableColumn<Lot, String> colNumLot;
    @FXML
    private TableColumn<Lot, Integer> colQuantite;
    @FXML
    private TableColumn<Lot, String> colDatePerem;
    @FXML
    private TableColumn<Lot, Double> colPrixAchat;

    @FXML
    private ToggleButton btnArchives;

    private final StockService stockService = ServiceLocator.getInstance().getStockService();
    private final MedicamentService medicamentService = ServiceLocator.getInstance().getMedicamentService();
    private final ObservableList<Lot> lots = FXCollections.observableArrayList();

    // Cache for logic
    private final Map<Integer, Integer> thresholds = new HashMap<>();
    private final Map<Integer, Integer> totalStocks = new HashMap<>();

    @FXML
    public void initialize() {
        setupRowFactory();
        colId.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getId()).asObject());
        colMedicament.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getNomMedicament()));
        colNumLot.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getNumeroLot()));
        colQuantite.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getQuantiteStock()).asObject());
        colDatePerem.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getDatePeremption().toString()));
        colPrixAchat.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleDoubleProperty(
                        cd.getValue().getPrixAchat() != null ? cd.getValue().getPrixAchat().doubleValue() : 0)
                        .asObject());

        chargerTousLesLots();
    }

    private void setupRowFactory() {
        tableLots.setRowFactory(tv -> new TableRow<Lot>() {
            @Override
            protected void updateItem(Lot item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    getStyleClass().removeAll("row-low-stock", "row-expiring");
                } else {
                    // Reset styles
                    getStyleClass().removeAll("row-low-stock", "row-expiring");

                    LocalDate now = LocalDate.now();
                    boolean isExpiring = item.getDatePeremption() != null &&
                                         item.getDatePeremption().isBefore(now.plusMonths(3));

                    int threshold = thresholds.getOrDefault(item.getMedicamentId(), 0);
                    int total = totalStocks.getOrDefault(item.getMedicamentId(), 0);
                    boolean isLowStock = total < threshold;

                    // Priority: Expiring (Yellow) > Low Stock (Orange)
                    // If expiring, use yellow. If low stock, use orange.
                    // User requirement: "si un lot est à la fois faible et proche de la péremption, choisis la couleur la plus critique"
                    // We assume Expiring is most critical for a specific Lot row.
                    if (isExpiring) {
                        getStyleClass().add("row-expiring");
                    } else if (isLowStock) {
                        getStyleClass().add("row-low-stock");
                    }
                }
            }
        });
    }

    @FXML
    private void handleActualiser() {
        chargerTousLesLots();
    }

    @FXML
    private void handleToggleArchives() {
        chargerTousLesLots();
    }

    private void chargerTousLesLots() {
        lots.clear();
        java.util.List<Lot> allLots = stockService.getAllLots();

        // Refresh caches
        thresholds.clear();
        medicamentService.getAllMedicaments().forEach(m ->
            thresholds.put(m.getId(), m.getSeuilMinAlerte())
        );

        totalStocks.clear();
        // Calculate total stock based on ALL lots (not just filtered ones)
        Map<Integer, Integer> calculatedStocks = allLots.stream()
                .collect(Collectors.groupingBy(Lot::getMedicamentId,
                         Collectors.summingInt(Lot::getQuantiteStock)));
        totalStocks.putAll(calculatedStocks);

        boolean showArchives = btnArchives != null && btnArchives.isSelected();

        if (showArchives) {
            // Mode Archives: seulement Quantité == 0
            allLots.stream()
                    .filter(l -> l.getQuantiteStock() == 0)
                    .forEach(lots::add);
        } else {
            // Mode par défaut: seulement Quantité > 0
            allLots.stream()
                    .filter(l -> l.getQuantiteStock() > 0)
                    .forEach(lots::add);
        }
        tableLots.setItems(lots);
        tableLots.refresh(); // Force refresh for row factory
    }
}
