package com.sgpa.controller;

import com.sgpa.model.Fournisseur;
import com.sgpa.service.FournisseurService;
import com.sgpa.util.FXUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class FournisseurController {

    @FXML private TableView<Fournisseur> tableFournisseurs;
    @FXML private TableColumn<Fournisseur, Integer> colId;
    @FXML private TableColumn<Fournisseur, String> colNom;
    @FXML private TableColumn<Fournisseur, String> colContact;
    @FXML private TableColumn<Fournisseur, String> colAdresse;

    @FXML private TextField txtNom;
    @FXML private TextField txtContact;
    @FXML private TextField txtAdresse;

    @FXML private Label lblMessage;

    private final FournisseurService service = new FournisseurService();
    private final ObservableList<Fournisseur> fournisseurs = FXCollections.observableArrayList();
    private Fournisseur selection;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().getId()).asObject());
        colNom.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getNom()));
        colContact.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getContact()));
        colAdresse.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getAdresse()));

        tableFournisseurs.setItems(fournisseurs);
        tableFournisseurs.getSelectionModel().selectedItemProperty().addListener((obs, ancien, nouveau) -> {
            selection = nouveau;
            if (nouveau != null) {
                txtNom.setText(nouveau.getNom());
                txtContact.setText(nouveau.getContact());
                txtAdresse.setText(nouveau.getAdresse() != null ? nouveau.getAdresse() : "");
            }
        });

        charger();
    }

    @FXML
    private void handleActualiser() {
        charger();
    }

    @FXML
    private void handleAjouter() {
        String nom = txtNom.getText().trim();
        String contact = txtContact.getText().trim();
        String adresse = txtAdresse.getText().trim();

        if (nom.isEmpty() || contact.isEmpty()) {
            afficherMessage("Veuillez remplir au moins le nom et le contact.", true);
            return;
        }

        service.creer(new Fournisseur(nom, contact, adresse));
        afficherMessage("Fournisseur \"" + nom + "\" ajouté avec succès.", false);
        reinitialiser();
        charger();
    }

    @FXML
    private void handleModifier() {
        if (selection == null) {
            afficherMessage("Sélectionnez un fournisseur dans le tableau pour le modifier.", true);
            return;
        }

        String nom = txtNom.getText().trim();
        String contact = txtContact.getText().trim();

        if (nom.isEmpty() || contact.isEmpty()) {
            afficherMessage("Le nom et le contact sont obligatoires.", true);
            return;
        }

        selection.setNom(nom);
        selection.setContact(contact);
        selection.setAdresse(txtAdresse.getText().trim());

        service.modifier(selection);
        afficherMessage("Fournisseur \"" + nom + "\" modifié avec succès.", false);
        charger();
    }

    @FXML
    private void handleSupprimer() {
        if (selection == null) {
            afficherMessage("Sélectionnez un fournisseur dans le tableau pour le supprimer.", true);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le fournisseur ?");
        confirm.setContentText("Voulez-vous vraiment supprimer \"" + selection.getNom() + "\" ?\n"
                + "Les commandes associées perdront leur référence fournisseur.");

        confirm.showAndWait().ifPresent(reponse -> {
            if (reponse == ButtonType.OK) {
                service.supprimer(selection.getId());
                afficherMessage("Fournisseur supprimé.", false);
                reinitialiser();
                charger();
            }
        });
    }

    @FXML
    private void handleReinitialiser() {
        reinitialiser();
    }

    // ── Méthodes internes ───────────────────────────────────────────────────

    private void charger() {
        fournisseurs.setAll(service.getAll());
        masquerMessage();
    }

    private void reinitialiser() {
        txtNom.clear();
        txtContact.clear();
        txtAdresse.clear();
        tableFournisseurs.getSelectionModel().clearSelection();
        selection = null;
        masquerMessage();
    }

    private void afficherMessage(String msg, boolean erreur) {
        lblMessage.setText(msg);
        lblMessage.getStyleClass().removeAll("message-error", "message-success");
        lblMessage.getStyleClass().add(erreur ? "message-error" : "message-success");
        lblMessage.setVisible(true);
        lblMessage.setManaged(true);
    }

    private void masquerMessage() {
        lblMessage.setVisible(false);
        lblMessage.setManaged(false);
    }
}
