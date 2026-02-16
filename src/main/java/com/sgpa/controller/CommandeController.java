package com.sgpa.controller;

import com.sgpa.model.Commande;
import com.sgpa.model.Fournisseur;
import com.sgpa.service.CommandeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CommandeController {

    @FXML
    private ComboBox<Fournisseur> cmbFournisseur;
    @FXML
    private TextField txtNomFournisseur;
    @FXML
    private TextField txtContactFournisseur;
    @FXML
    private TextField txtAdresseFournisseur;

    @FXML
    private TableView<Commande> tableCommandes;
    @FXML
    private TableColumn<Commande, Integer> colId;
    @FXML
    private TableColumn<Commande, String> colFournisseur;
    @FXML
    private TableColumn<Commande, String> colDate;
    @FXML
    private TableColumn<Commande, String> colStatut;

    private CommandeService commandeService = new CommandeService();
    private ObservableList<Commande> commandes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Charger les fournisseurs
        chargerFournisseurs();

        // Configuration du tableau
        colId.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colFournisseur.setCellValueFactory(cellData -> {
            int fournisseurId = cellData.getValue().getFournisseurId();
            // Chercher le fournisseur dans la liste
            for (Fournisseur f : commandeService.getAllFournisseurs()) {
                if (f.getId() == fournisseurId) {
                    return new javafx.beans.property.SimpleStringProperty(f.getNom());
                }
            }
            return new javafx.beans.property.SimpleStringProperty("?");
        });
        colDate.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getDateCommande().toString()));
        colStatut.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatut()));

        chargerCommandes();
    }

    @FXML
    private void handleAjouterFournisseur() {
        String nom = txtNomFournisseur.getText();
        String contact = txtContactFournisseur.getText();
        String adresse = txtAdresseFournisseur.getText();

        if (nom.isEmpty() || contact.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir au moins le nom et le contact.", Alert.AlertType.WARNING);
            return;
        }

        Fournisseur fournisseur = new Fournisseur(nom, contact, adresse);
        commandeService.ajouterFournisseur(fournisseur);

        showAlert("Succès", "Fournisseur ajouté avec succès !", Alert.AlertType.INFORMATION);
        viderChampsFournisseur();
        chargerFournisseurs();
    }

    @FXML
    private void handleCreerCommande() {
        Fournisseur fournisseur = cmbFournisseur.getValue();
        if (fournisseur == null) {
            showAlert("Erreur", "Veuillez sélectionner un fournisseur.", Alert.AlertType.WARNING);
            return;
        }

        // Créer une commande vide (sans lignes pour l'instant)
        commandeService.creerCommande(fournisseur.getId(), new java.util.ArrayList<>());

        showAlert("Succès",
                "Commande créée avec succès !\n\nNote: Utilisez la console pour ajouter des lignes de commande.",
                Alert.AlertType.INFORMATION);
        chargerCommandes();
    }

    @FXML
    private void handleActualiser() {
        chargerCommandes();
    }

    private void chargerFournisseurs() {
        ObservableList<Fournisseur> fournisseurs = FXCollections.observableArrayList(
                commandeService.getAllFournisseurs());
        cmbFournisseur.setItems(fournisseurs);

        cmbFournisseur.setCellFactory(param -> new ListCell<Fournisseur>() {
            @Override
            protected void updateItem(Fournisseur item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });

        cmbFournisseur.setButtonCell(new ListCell<Fournisseur>() {
            @Override
            protected void updateItem(Fournisseur item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
    }

    private void chargerCommandes() {
        commandes.clear();
        commandes.addAll(commandeService.getAllCommandes());
        tableCommandes.setItems(commandes);
    }

    private void viderChampsFournisseur() {
        txtNomFournisseur.clear();
        txtContactFournisseur.clear();
        txtAdresseFournisseur.clear();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
