package com.sgpa.controller;

import com.sgpa.dao.impl.LotDAOImpl;
import com.sgpa.model.Lot;
import com.sgpa.model.Medicament;
import com.sgpa.service.MedicamentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

public class StockController {

    @FXML
    private ComboBox<Medicament> cmbMedicament;
    @FXML
    private TextField txtNumeroLot;
    @FXML
    private TextField txtQuantite;
    @FXML
    private DatePicker dpDatePeremption;
    @FXML
    private TextField txtPrixAchat;

    @FXML
    private TableView<Lot> tableLots;
    @FXML
    private TableColumn<Lot, Integer> colId;
    @FXML
    private TableColumn<Lot, String> colMedicament; // Nouvelle colonne
    @FXML
    private TableColumn<Lot, String> colNumLot;
    @FXML
    private TableColumn<Lot, Integer> colQuantite;
    @FXML
    private TableColumn<Lot, String> colDatePerem;
    @FXML
    private TableColumn<Lot, Double> colPrixAchat;

    private MedicamentService medicamentService = new MedicamentService();
    private LotDAOImpl lotDAO = new LotDAOImpl();
    private ObservableList<Lot> lots = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Charger les médicaments
        ObservableList<Medicament> medicaments = FXCollections.observableArrayList(
                medicamentService.getAllMedicaments());
        cmbMedicament.setItems(medicaments);

        cmbMedicament.setCellFactory(param -> new ListCell<Medicament>() {
            @Override
            protected void updateItem(Medicament item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNomCommercial());
            }
        });

        cmbMedicament.setButtonCell(new ListCell<Medicament>() {
            @Override
            protected void updateItem(Medicament item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNomCommercial());
            }
        });

        // Configuration du tableau
        colId.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        // Configuration de la colonne Médicament
        colMedicament.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomMedicament()));

        colNumLot.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNumeroLot()));
        colQuantite.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getQuantiteStock())
                        .asObject());
        colDatePerem.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDatePeremption().toString()));
        colPrixAchat.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleDoubleProperty(cellData.getValue().getPrixAchat())
                        .asObject());

        chargerTousLesLots();
    }

    @FXML
    private void handleAjouterLot() {
        Medicament medicament = cmbMedicament.getValue();
        if (medicament == null) {
            showAlert("Erreur", "Veuillez sélectionner un médicament.", Alert.AlertType.WARNING);
            return;
        }

        try {
            String numeroLot = txtNumeroLot.getText();
            int quantite = Integer.parseInt(txtQuantite.getText());
            LocalDate datePeremption = dpDatePeremption.getValue();
            double prixAchat = Double.parseDouble(txtPrixAchat.getText());

            if (numeroLot.isEmpty() || datePeremption == null) {
                showAlert("Erreur", "Veuillez remplir tous les champs.", Alert.AlertType.WARNING);
                return;
            }

            Lot lot = new Lot(medicament.getId(), numeroLot, quantite, datePeremption, prixAchat);
            lotDAO.create(lot);

            showAlert("Succès", "Lot ajouté avec succès !", Alert.AlertType.INFORMATION);
            viderChamps();
            chargerTousLesLots();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs valides.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleNouveau() {
        viderChamps();
    }

    @FXML
    private void handleActualiser() {
        chargerTousLesLots();
    }

    private void chargerTousLesLots() {
        lots.clear();
        lots.addAll(lotDAO.findAll());
        tableLots.setItems(lots);
    }

    private void viderChamps() {
        cmbMedicament.setValue(null);
        txtNumeroLot.clear();
        txtQuantite.clear();
        dpDatePeremption.setValue(null);
        txtPrixAchat.clear();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
