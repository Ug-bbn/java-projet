package com.sgpa.controller;

import com.sgpa.model.Medicament;
import com.sgpa.model.Vente;
import com.sgpa.service.MedicamentService;
import com.sgpa.service.VenteService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class VenteController {

    @FXML
    private ComboBox<Medicament> cmbMedicament;
    @FXML
    private TextField txtQuantite;
    @FXML
    private CheckBox chkOrdonnance;
    @FXML
    private Label lblStockDisponible;

    @FXML
    private TableView<Vente> tableHistorique;
    @FXML
    private TableColumn<Vente, Integer> colId;
    @FXML
    private TableColumn<Vente, String> colMedicaments; // Nouvelle colonne
    @FXML
    private TableColumn<Vente, String> colDate;
    @FXML
    private TableColumn<Vente, Double> colTotal;
    @FXML
    private TableColumn<Vente, Boolean> colOrdonnance;

    private MedicamentService medicamentService = new MedicamentService();
    private VenteService venteService = new VenteService();
    private ObservableList<Vente> ventes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Charger les médicaments dans le ComboBox
        ObservableList<Medicament> medicaments = FXCollections.observableArrayList(
                medicamentService.getAllMedicaments());
        cmbMedicament.setItems(medicaments);

        // Afficher le nom du médicament dans le ComboBox
        cmbMedicament.setCellFactory(param -> new ListCell<Medicament>() {
            @Override
            protected void updateItem(Medicament item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNomCommercial() + " - " + item.getPrixPublic() + "€");
                }
            }
        });

        cmbMedicament.setButtonCell(new ListCell<Medicament>() {
            @Override
            protected void updateItem(Medicament item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNomCommercial() + " - " + item.getPrixPublic() + "€");
                }
            }
        });

        // Listener pour afficher le stock disponible
        cmbMedicament.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int stock = medicamentService.getStockTotal(newVal.getId());
                lblStockDisponible.setText("Stock disponible : " + stock);
            } else {
                lblStockDisponible.setText("");
            }
        });

        // Configuration du tableau d'historique
        colId.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        // Configuration de la colonne Médicaments
        colMedicaments.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNomsMedicaments()));

        colDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateVente().toString()));
        colTotal.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleDoubleProperty(
                        cellData.getValue().getTotalVente() != null ? cellData.getValue().getTotalVente().doubleValue() : 0)
                        .asObject());
        colOrdonnance.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isSurOrdonnance())
                        .asObject());

        chargerHistorique();
    }

    @FXML
    private void handleEnregistrerVente() {
        Medicament medicament = cmbMedicament.getValue();
        if (medicament == null) {
            showAlert("Erreur", "Veuillez sélectionner un médicament.", Alert.AlertType.WARNING);
            return;
        }

        try {
            int quantite = Integer.parseInt(txtQuantite.getText());
            if (quantite <= 0) {
                showAlert("Erreur", "La quantité doit être supérieure à 0.", Alert.AlertType.WARNING);
                return;
            }

            boolean surOrdonnance = chkOrdonnance.isSelected();
            boolean success = venteService.enregistrerVente(medicament.getId(), quantite, surOrdonnance);

            if (success) {
                showAlert("Succès", "Vente enregistrée avec succès !", Alert.AlertType.INFORMATION);
                viderChamps();
                chargerHistorique();

                // Mettre à jour le stock affiché
                if (cmbMedicament.getValue() != null) {
                    int stock = medicamentService.getStockTotal(cmbMedicament.getValue().getId());
                    lblStockDisponible.setText("Stock disponible : " + stock);
                }
            } else {
                showAlert("Erreur", "Stock insuffisant pour cette vente.", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer une quantité valide.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleNouveau() {
        viderChamps();
    }

    private void chargerHistorique() {
        ventes.clear();
        ventes.addAll(venteService.getHistoriqueVentes());
        tableHistorique.setItems(ventes);
    }

    private void viderChamps() {
        cmbMedicament.setValue(null);
        txtQuantite.clear();
        chkOrdonnance.setSelected(false);
        lblStockDisponible.setText("");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
