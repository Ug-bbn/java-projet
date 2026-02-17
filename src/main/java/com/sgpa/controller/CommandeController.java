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
import java.time.LocalDate;
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

    @FXML
    private void handleRecevoirCommande() {
        Commande selected = tableCommandes.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Veuillez s\u00e9lectionner une commande.", Alert.AlertType.WARNING);
            return;
        }
        if (com.sgpa.model.StatutCommande.RECUE.equals(selected.getStatut())) {
            showAlert("Information", "Cette commande a d\u00e9j\u00e0 \u00e9t\u00e9 re\u00e7ue.", Alert.AlertType.INFORMATION);
            return;
        }

        // Dialog to input lot number and expiry date
        Dialog<javafx.util.Pair<String, LocalDate>> dialog = new Dialog<>();
        dialog.setTitle("R\u00e9ception de commande");
        dialog.setHeaderText("Commande #" + selected.getId() + " - Saisir les informations du lot");

        ButtonType btnValider = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField txtNumeroLot = new TextField();
        txtNumeroLot.setPromptText("Ex: LOT-2026-001");
        DatePicker dpPeremption = new DatePicker(LocalDate.now().plusYears(1));

        grid.add(new Label("Num\u00e9ro de lot:"), 0, 0);
        grid.add(txtNumeroLot, 1, 0);
        grid.add(new Label("Date de p\u00e9remption:"), 0, 1);
        grid.add(dpPeremption, 1, 1);

        dialog.getDialogPane().setContent(grid);
        txtNumeroLot.requestFocus();

        dialog.setResultConverter(btn -> {
            if (btn == btnValider) {
                return new javafx.util.Pair<>(txtNumeroLot.getText().trim(), dpPeremption.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            String numeroLot = result.getKey();
            LocalDate datePeremption = result.getValue();

            if (numeroLot.isEmpty()) {
                showAlert("Erreur", "Le num\u00e9ro de lot est obligatoire.", Alert.AlertType.WARNING);
                return;
            }
            if (datePeremption == null || datePeremption.isBefore(LocalDate.now())) {
                showAlert("Erreur", "La date de p\u00e9remption doit \u00eatre dans le futur.", Alert.AlertType.WARNING);
                return;
            }

            try {
                commandeService.recevoirCommande(selected.getId(), numeroLot, datePeremption);
                showAlert("Succ\u00e8s", "Commande #" + selected.getId() + " r\u00e9ceptionn\u00e9e avec succ\u00e8s !\nLots cr\u00e9\u00e9s et stock mis \u00e0 jour.", Alert.AlertType.INFORMATION);
                chargerCommandes();
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la r\u00e9ception : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    private void handleModifierFournisseur() {
        Fournisseur selected = cmbFournisseur.getValue();
        if (selected == null) {
            showAlert("Erreur", "Veuillez s\u00e9lectionner un fournisseur \u00e0 modifier.", Alert.AlertType.WARNING);
            return;
        }

        Dialog<Fournisseur> dialog = new Dialog<>();
        dialog.setTitle("Modifier Fournisseur");
        dialog.setHeaderText("Modifier : " + selected.getNom());

        ButtonType btnValider = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField txtNom = new TextField(selected.getNom());
        TextField txtContact = new TextField(selected.getContact());
        TextField txtAdresse = new TextField(selected.getAdresse());

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(txtNom, 1, 0);
        grid.add(new Label("Contact:"), 0, 1);
        grid.add(txtContact, 1, 1);
        grid.add(new Label("Adresse:"), 0, 2);
        grid.add(txtAdresse, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == btnValider) {
                selected.setNom(txtNom.getText().trim());
                selected.setContact(txtContact.getText().trim());
                selected.setAdresse(txtAdresse.getText().trim());
                return selected;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(f -> {
            if (f.getNom().isEmpty()) {
                showAlert("Erreur", "Le nom du fournisseur est obligatoire.", Alert.AlertType.WARNING);
                return;
            }
            commandeService.modifierFournisseur(f);
            showAlert("Succ\u00e8s", "Fournisseur modifi\u00e9 avec succ\u00e8s.", Alert.AlertType.INFORMATION);
            chargerFournisseurs();
        });
    }

    @FXML
    private void handleSupprimerFournisseur() {
        Fournisseur selected = cmbFournisseur.getValue();
        if (selected == null) {
            showAlert("Erreur", "Veuillez s\u00e9lectionner un fournisseur \u00e0 supprimer.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le fournisseur ?");
        confirm.setContentText("Voulez-vous vraiment supprimer \"" + selected.getNom() + "\" ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                commandeService.supprimerFournisseur(selected.getId());
                showAlert("Succ\u00e8s", "Fournisseur supprim\u00e9.", Alert.AlertType.INFORMATION);
                cmbFournisseur.setValue(null);
                chargerFournisseurs();
            }
        });
    }

    // ========================================================
    // PRIVATE HELPERS
    // ========================================================

    private void chargerFournisseurs() {
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();
        fournisseurCache = commandeService.getAllFournisseurs().stream()
                .filter(f -> seen.add(f.getNom()))
                .collect(java.util.stream.Collectors.toList());
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
