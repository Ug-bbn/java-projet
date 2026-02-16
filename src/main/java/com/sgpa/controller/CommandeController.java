package com.sgpa.controller;

import com.sgpa.model.Commande;
import com.sgpa.model.Fournisseur;
import com.sgpa.model.LigneCommande;
import com.sgpa.model.Medicament;
import com.sgpa.service.CommandeService;
import com.sgpa.service.MedicamentService;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class CommandeController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    // --- Fournisseur form ---
    @FXML
    private ComboBox<Fournisseur> cmbFournisseur;
    @FXML
    private TextField txtNomFournisseur;
    @FXML
    private TextField txtContactFournisseur;
    @FXML
    private TextField txtAdresseFournisseur;

    // --- Article form ---
    @FXML
    private ComboBox<Medicament> cmbMedicament;
    @FXML
    private TextField txtQuantite;
    @FXML
    private TextField txtPrixUnitaire;

    // --- Articles table ---
    @FXML
    private TableView<LigneCommande> tableArticles;
    @FXML
    private TableColumn<LigneCommande, String> colArticleMedicament;
    @FXML
    private TableColumn<LigneCommande, Integer> colArticleQuantite;
    @FXML
    private TableColumn<LigneCommande, String> colArticlePrix;
    @FXML
    private TableColumn<LigneCommande, String> colArticleTotal;
    @FXML
    private Label lblTotalCommande;

    // --- Commandes table ---
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
    @FXML
    private TableColumn<Commande, Integer> colNbArticles;
    @FXML
    private TableColumn<Commande, String> colTotal;

    // --- Services & data ---
    private final CommandeService commandeService = new CommandeService();
    private final MedicamentService medicamentService = new MedicamentService();
    private final ObservableList<Commande> commandes = FXCollections.observableArrayList();
    private final ObservableList<LigneCommande> articlesEnCours = FXCollections.observableArrayList();

    // Cached lists to avoid querying DB on every cell render
    private java.util.List<Fournisseur> fournisseurCache;
    private java.util.List<Medicament> medicamentCache;

    // ========================================================
    // INITIALISATION
    // ========================================================
    @FXML
    public void initialize() {
        chargerFournisseurs();
        chargerMedicaments();

        // --- Input Validation ---
        txtQuantite.setTextFormatter(new TextFormatter<>(change -> {
            return change.getControlNewText().matches("\\d*") ? change : null;
        }));

        txtPrixUnitaire.setTextFormatter(new TextFormatter<>(change -> {
            return change.getControlNewText().matches("\\d*(\\.\\d*)?") ? change : null;
        }));

        // --- Commandes table columns ---
        colId.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getId()).asObject());

        colFournisseur.setCellValueFactory(cd -> {
            int fid = cd.getValue().getFournisseurId();
            String nom = fournisseurCache.stream()
                    .filter(f -> f.getId() == fid)
                    .map(Fournisseur::getNom)
                    .findFirst().orElse("?");
            return new javafx.beans.property.SimpleStringProperty(nom);
        });

        colDate.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getDateCommande().format(DATE_FMT)));

        colStatut.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getStatut().getLabel()));

        colStatut.setCellFactory(column -> new TableCell<Commande, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label lbl = new Label(item);
                    // Map raw status or label to friendly text/color
                    String upper = item.toUpperCase();
                    if (upper.contains("EN COURS") || upper.contains("ATTENTE")) {
                        lbl.getStyleClass().add("badge-warning");
                    } else if (upper.contains("LIVR") || upper.contains("VALID")) {
                        lbl.getStyleClass().add("badge-success");
                    } else {
                        lbl.getStyleClass().add("badge-danger");
                    }
                    setGraphic(lbl);
                    setText(null);
                }
            }
        });

        colNbArticles.setCellValueFactory(cd -> {
            int count = commandeService.getLignesCommande(cd.getValue().getId()).size();
            return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
        });

        colTotal.setCellValueFactory(cd -> {
            java.util.List<LigneCommande> lignes = commandeService.getLignesCommande(cd.getValue().getId());
            BigDecimal total = lignes.stream()
                    .map(LigneCommande::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return new javafx.beans.property.SimpleStringProperty(total.toPlainString() + " €");
        });
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        // --- Articles table columns ---
        colArticleMedicament.setCellValueFactory(cd -> {
            int medId = cd.getValue().getMedicamentId();
            String nom = medicamentCache.stream()
                    .filter(m -> m.getId() == medId)
                    .map(Medicament::getNomCommercial)
                    .findFirst().orElse("?");
            return new javafx.beans.property.SimpleStringProperty(nom);
        });

        colArticleQuantite.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getQuantite()).asObject());

        colArticlePrix.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getPrixUnitaire().toPlainString() + " €"));
        colArticlePrix.setStyle("-fx-alignment: CENTER-RIGHT;");

        colArticleTotal.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getTotal().toPlainString() + " €"));
        colArticleTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        tableArticles.setItems(articlesEnCours);

        // Update total label whenever articles list changes
        articlesEnCours.addListener((ListChangeListener<LigneCommande>) c -> updateTotalLabel());
        updateTotalLabel();

        chargerCommandes();
    }

    // ========================================================
    // HANDLERS
    // ========================================================

    @FXML
    private void handleAjouterFournisseur() {
        String nom = txtNomFournisseur.getText().trim();
        String contact = txtContactFournisseur.getText().trim();
        String adresse = txtAdresseFournisseur.getText().trim();

        if (nom.isEmpty() || contact.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir au moins le nom et le contact.", Alert.AlertType.WARNING);
            return;
        }

        commandeService.ajouterFournisseur(new Fournisseur(nom, contact, adresse));
        showAlert("Succès", "Fournisseur ajouté avec succès !", Alert.AlertType.INFORMATION);
        viderChampsFournisseur();
        chargerFournisseurs();
    }

    @FXML
    private void handleAjouterArticle() {
        Medicament medicament = cmbMedicament.getValue();
        if (medicament == null) {
            showAlert("Erreur", "Veuillez sélectionner un médicament.", Alert.AlertType.WARNING);
            return;
        }

        int quantite;
        try {
            quantite = Integer.parseInt(txtQuantite.getText().trim());
            if (quantite <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La quantité doit être un entier positif.", Alert.AlertType.WARNING);
            return;
        }

        BigDecimal prixUnitaire;
        try {
            prixUnitaire = new BigDecimal(txtPrixUnitaire.getText().trim());
            if (prixUnitaire.compareTo(BigDecimal.ZERO) <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Le prix unitaire doit être un nombre positif.", Alert.AlertType.WARNING);
            return;
        }

        articlesEnCours.add(new LigneCommande(medicament.getId(), quantite, prixUnitaire));
        txtQuantite.clear();
        txtPrixUnitaire.clear();
        cmbMedicament.getSelectionModel().clearSelection();
        cmbMedicament.requestFocus();
    }

    @FXML
    private void handleRetirerArticle() {
        LigneCommande selected = tableArticles.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Veuillez sélectionner un article à retirer.", Alert.AlertType.WARNING);
            return;
        }
        articlesEnCours.remove(selected);
    }

    @FXML
    private void handleCreerCommande() {
        Fournisseur fournisseur = cmbFournisseur.getValue();
        if (fournisseur == null) {
            showAlert("Erreur", "Veuillez sélectionner un fournisseur.", Alert.AlertType.WARNING);
            return;
        }
        if (articlesEnCours.isEmpty()) {
            showAlert("Erreur", "Veuillez ajouter au moins un article à la commande.", Alert.AlertType.WARNING);
            return;
        }

        commandeService.creerCommande(fournisseur.getId(), new java.util.ArrayList<>(articlesEnCours));

        BigDecimal total = articlesEnCours.stream()
                .map(LigneCommande::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        showAlert("Succès",
                String.format("Commande créée avec succès !\n%d article(s) — Total : %s €",
                        articlesEnCours.size(), total.toPlainString()),
                Alert.AlertType.INFORMATION);

        articlesEnCours.clear();
        chargerCommandes();
    }

    @FXML
    private void handleActualiser() {
        chargerCommandes();
        chargerFournisseurs();
        chargerMedicaments();
    }

    // ========================================================
    // PRIVATE HELPERS
    // ========================================================

    private void chargerFournisseurs() {
        fournisseurCache = commandeService.getAllFournisseurs();
        ObservableList<Fournisseur> list = FXCollections.observableArrayList(fournisseurCache);
        cmbFournisseur.setItems(list);

        cmbFournisseur.setCellFactory(p -> new ListCell<>() {
            @Override
            protected void updateItem(Fournisseur item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
        cmbFournisseur.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Fournisseur item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNom());
            }
        });
    }

    private void chargerMedicaments() {
        medicamentCache = medicamentService.getAllMedicaments();
        ObservableList<Medicament> list = FXCollections.observableArrayList(medicamentCache);
        cmbMedicament.setItems(list);

        cmbMedicament.setCellFactory(p -> new ListCell<>() {
            @Override
            protected void updateItem(Medicament item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getNomCommercial() + " (" + item.getDosage() + ")");
            }
        });
        cmbMedicament.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Medicament item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getNomCommercial() + " (" + item.getDosage() + ")");
            }
        });
    }

    private void chargerCommandes() {
        commandes.clear();
        commandes.addAll(commandeService.getAllCommandes());
        tableCommandes.setItems(commandes);
    }

    private void updateTotalLabel() {
        BigDecimal total = articlesEnCours.stream()
                .map(LigneCommande::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalCommande.setText(articlesEnCours.isEmpty()
                ? ""
                : String.format("Total : %s €  (%d article%s)",
                        total.toPlainString(),
                        articlesEnCours.size(),
                        articlesEnCours.size() > 1 ? "s" : ""));
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
