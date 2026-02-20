package com.sgpa.controller;

import com.sgpa.model.Lot;
import com.sgpa.model.Medicament;
import com.sgpa.service.StockService;
import com.sgpa.service.MedicamentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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

    private final StockService stockService = new StockService();
    private final MedicamentService medicamentService = new MedicamentService();
    private final ObservableList<Lot> lots = FXCollections.observableArrayList();

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
                getStyleClass().remove("row-low-stock");
                getStyleClass().remove("row-expiring");
                setStyle("");

                if (item == null || empty) {
                    return;
                }

                LocalDate now = LocalDate.now();
                boolean isExpiring = item.getDatePeremption() != null &&
                                     item.getDatePeremption().isBefore(now.plusMonths(3));

                int threshold = thresholds.getOrDefault(item.getMedicamentId(), 0);
                int total = totalStocks.getOrDefault(item.getMedicamentId(), 0);
                boolean isLowStock = threshold > 0 && total < threshold;

                if (isExpiring) {
                    getStyleClass().add("row-expiring");
                } else if (isLowStock) {
                    getStyleClass().add("row-low-stock");
                }
            }
        });
    }

    @FXML
    private void handleShowLegende() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Légende des couleurs");
        alert.setHeaderText("Code couleur du tableau de stock");
        alert.setContentText(
            "Orange = Stock Faible (quantité totale du médicament < seuil minimum)\n" +
            "Jaune = Périme bientôt (date de péremption dans moins de 3 mois)");
        alert.showAndWait();
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

        thresholds.clear();
        medicamentService.getAllMedicaments().forEach(m ->
            thresholds.put(m.getId(), m.getSeuilMinAlerte())
        );

        totalStocks.clear();
        Map<Integer, Integer> calculatedStocks = allLots.stream()
                .collect(Collectors.groupingBy(Lot::getMedicamentId,
                         Collectors.summingInt(Lot::getQuantiteStock)));
        totalStocks.putAll(calculatedStocks);

        boolean showArchives = btnArchives != null && btnArchives.isSelected();

        if (showArchives) {
            allLots.stream()
                    .filter(l -> l.getQuantiteStock() == 0)
                    .forEach(lots::add);
        } else {
            allLots.stream()
                    .filter(l -> l.getQuantiteStock() > 0)
                    .forEach(lots::add);
        }
        tableLots.setItems(lots);
        tableLots.refresh(); // Force refresh for row factory
    }
}
