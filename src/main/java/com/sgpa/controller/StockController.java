package com.sgpa.controller;

import com.sgpa.model.Lot;
import com.sgpa.service.StockService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    private final ObservableList<Lot> lots = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
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
    }
}
