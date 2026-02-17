package com.sgpa.controller;

import com.sgpa.model.Medicament;
import com.sgpa.model.Lot;
import com.sgpa.service.MedicamentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class AlerteController {

    @FXML private TableView<Medicament> tableStockMin;
    @FXML private TableColumn<Medicament, String> colNomStock;
    @FXML private TableColumn<Medicament, Integer> colStockActuel;
    @FXML private TableColumn<Medicament, Integer> colSeuilMin;

    @FXML private TableView<LotPeremption> tablePeremption;
    @FXML private TableColumn<LotPeremption, String> colNomPerem;
    @FXML private TableColumn<LotPeremption, String> colNumLot;
    @FXML private TableColumn<LotPeremption, Integer> colQuantite;
    @FXML private TableColumn<LotPeremption, String> colDatePerem;

    private MedicamentService service = new MedicamentService();

    @FXML
    public void initialize() {
        // Configuration colonnes stock minimum
        colNomStock.setCellValueFactory(new PropertyValueFactory<>("nomCommercial"));
        colStockActuel.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(
                service.getStockTotal(cellData.getValue().getId())
            ).asObject()
        );
        colSeuilMin.setCellValueFactory(new PropertyValueFactory<>("seuilMinAlerte"));

        // Configuration colonnes péremption
        colNomPerem.setCellValueFactory(new PropertyValueFactory<>("nomMedicament"));
        colNumLot.setCellValueFactory(new PropertyValueFactory<>("numeroLot"));
        colQuantite.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        colDatePerem.setCellValueFactory(new PropertyValueFactory<>("datePeremption"));

        chargerAlertes();
    }

    @FXML
    private void handleActualiser() {
        chargerAlertes();
    }

    private void chargerAlertes() {
        // Alertes stock minimum - dédupliquer par ID médicament
        java.util.List<Medicament> allAlertes = service.getMedicamentsEnAlerteStock();
        Set<Integer> seenIds = new HashSet<>();
        ObservableList<Medicament> alertesStock = FXCollections.observableArrayList(
            allAlertes.stream()
                .filter(m -> seenIds.add(m.getId()))
                .collect(Collectors.toList())
        );
        tableStockMin.setItems(alertesStock);

        // Alertes péremption
        ObservableList<LotPeremption> alertesPerem = FXCollections.observableArrayList();
        for (Lot lot : service.getLotsProchesPeremption()) {
            String nom = (lot.getNomMedicament() != null && !lot.getNomMedicament().isEmpty())
                    ? lot.getNomMedicament()
                    : "?";
            alertesPerem.add(new LotPeremption(
                nom,
                lot.getNumeroLot(),
                lot.getQuantiteStock(),
                lot.getDatePeremption().toString()
            ));
        }
        tablePeremption.setItems(alertesPerem);
    }

    // Classe interne pour afficher les lots avec péremption
    public static class LotPeremption {
        private final String nomMedicament;
        private final String numeroLot;
        private final int quantite;
        private final String datePeremption;

        public LotPeremption(String nomMedicament, String numeroLot, int quantite, String datePeremption) {
            this.nomMedicament = nomMedicament;
            this.numeroLot = numeroLot;
            this.quantite = quantite;
            this.datePeremption = datePeremption;
        }

        public String getNomMedicament() { return nomMedicament; }
        public String getNumeroLot() { return numeroLot; }
        public int getQuantite() { return quantite; }
        public String getDatePeremption() { return datePeremption; }
    }
}
