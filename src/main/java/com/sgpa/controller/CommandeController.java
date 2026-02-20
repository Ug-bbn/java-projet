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

import com.sgpa.util.FXUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CommandeController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private ComboBox<Fournisseur> cmbFournisseur;

    @FXML
    private ComboBox<Medicament> cmbMedicament;
    @FXML
    private TextField txtQuantite;
    @FXML
    private TextField txtPrixUnitaire;

    @FXML
    private TableView<LigneCommande> tableArticles;
    @FXML
    private TableColumn<LigneCommande, String> colArticleMedicament;
    @FXML
    private TableColumn<LigneCommande, String> colArticleForme;
    @FXML
    private TableColumn<LigneCommande, Integer> colArticleQuantite;
    @FXML
    private TableColumn<LigneCommande, String> colArticlePrix;
    @FXML
    private TableColumn<LigneCommande, String> colArticleTotal;
    @FXML
    private Label lblTotalCommande;

    @FXML
    private TableView<Commande> tableCommandes;
    @FXML
    private TableColumn<Commande, Integer> colId;
    @FXML
    private TableColumn<Commande, String> colMedicaments;
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

    private final CommandeService commandeService = new CommandeService();
    private final MedicamentService medicamentService = new MedicamentService();
    private final ObservableList<Commande> commandes = FXCollections.observableArrayList();
    private final ObservableList<LigneCommande> articlesEnCours = FXCollections.observableArrayList();

    private java.util.List<Fournisseur> fournisseurCache;
    private java.util.List<Medicament> medicamentCache;
    private final Map<Integer, Integer> lignesCountCache = new HashMap<>();
    private final Map<Integer, BigDecimal> lignesTotalCache = new HashMap<>();
    private final Map<Integer, String> lignesMedsCache = new HashMap<>();

    @FXML
    public void initialize() {
        chargerFournisseurs();
        chargerMedicaments();

        txtQuantite.setTextFormatter(new TextFormatter<>(change -> {
            return change.getControlNewText().matches("\\d*") ? change : null;
        }));

        txtPrixUnitaire.setTextFormatter(new TextFormatter<>(change -> {
            return change.getControlNewText().matches("\\d*(\\.\\d*)?") ? change : null;
        }));

        colId.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getId()).asObject());

        colMedicaments.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(
                        lignesMedsCache.getOrDefault(cd.getValue().getId(), "")));

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
                    String upper = item.toUpperCase();
                    if (upper.contains("EN COURS") || upper.contains("ATTENTE")) {
                        lbl.getStyleClass().add("badge-warning");
                    } else if (upper.contains("LIVR") || upper.contains("VALID") || upper.contains("RECUE")) {
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
            int count = lignesCountCache.getOrDefault(cd.getValue().getId(), 0);
            return new javafx.beans.property.SimpleIntegerProperty(count).asObject();
        });

        colTotal.setCellValueFactory(cd -> {
            BigDecimal total = lignesTotalCache.getOrDefault(cd.getValue().getId(), BigDecimal.ZERO);
            return new javafx.beans.property.SimpleStringProperty(total.toPlainString() + " €");
        });
        colTotal.getStyleClass().add("column-align-right-bold");

        colArticleMedicament.setCellValueFactory(cd -> {
            int medId = cd.getValue().getMedicamentId();
            String nom = medicamentCache.stream()
                    .filter(m -> m.getId() == medId)
                    .map(Medicament::getNomCommercial)
                    .findFirst().orElse("?");
            return new javafx.beans.property.SimpleStringProperty(nom);
        });

        colArticleForme.setCellValueFactory(cd -> {
            int medId = cd.getValue().getMedicamentId();
            String forme = medicamentCache.stream()
                    .filter(m -> m.getId() == medId)
                    .map(Medicament::getFormeGalenique)
                    .findFirst().orElse("-");
            return new javafx.beans.property.SimpleStringProperty(forme);
        });

        colArticleQuantite.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getQuantite()).asObject());

        colArticlePrix.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getPrixUnitaire().toPlainString() + " €"));
        colArticlePrix.getStyleClass().add("column-align-right");

        colArticleTotal.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getTotal().toPlainString() + " €"));
        colArticleTotal.getStyleClass().add("column-align-right-bold");

        tableArticles.setItems(articlesEnCours);

        articlesEnCours.addListener((ListChangeListener<LigneCommande>) c -> updateTotalLabel());
        updateTotalLabel();

        chargerCommandes();
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

        Dialog<LocalDate> dialog = new Dialog<>();
        dialog.setTitle("R\u00e9ception de commande");
        dialog.setHeaderText("Commande #" + selected.getId() + " - Date de p\u00e9remption du lot");

        ButtonType btnValider = new ButtonType("Valider", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnValider, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        DatePicker dpPeremption = new DatePicker(LocalDate.now().plusYears(1));

        grid.add(new Label("Date de p\u00e9remption:"), 0, 0);
        grid.add(dpPeremption, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dpPeremption.requestFocus();

        dialog.setResultConverter(btn -> btn == btnValider ? dpPeremption.getValue() : null);

        dialog.showAndWait().ifPresent(datePeremption -> {
            if (datePeremption == null || datePeremption.isBefore(LocalDate.now())) {
                showAlert("Erreur", "La date de p\u00e9remption doit \u00eatre dans le futur.", Alert.AlertType.WARNING);
                return;
            }

            try {
                commandeService.recevoirCommande(selected.getId(), datePeremption);
                showAlert("Succ\u00e8s", "Commande #" + selected.getId() + " r\u00e9ceptionn\u00e9e avec succ\u00e8s !\nLots cr\u00e9\u00e9s et stock mis \u00e0 jour.", Alert.AlertType.INFORMATION);
                chargerCommandes();
            } catch (Exception e) {
                showAlert("Erreur", "Erreur lors de la r\u00e9ception : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        });
    }

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
                        : item.getNomCommercial() + " (" + item.getFormeGalenique() + ")");
            }
        });
        cmbMedicament.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Medicament item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getNomCommercial() + " (" + item.getFormeGalenique() + ")");
            }
        });
    }

    private void chargerCommandes() {
        commandes.clear();
        lignesCountCache.clear();
        lignesTotalCache.clear();
        lignesMedsCache.clear();

        java.util.List<Commande> allCommandes = commandeService.getAllCommandes();
        for (Commande cmd : allCommandes) {
            java.util.List<LigneCommande> lignes = commandeService.getLignesCommande(cmd.getId());
            lignesCountCache.put(cmd.getId(), lignes.size());
            BigDecimal total = lignes.stream()
                    .map(LigneCommande::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            lignesTotalCache.put(cmd.getId(), total);

            String medsNames = lignes.stream()
                    .map(l -> medicamentCache.stream()
                            .filter(m -> m.getId() == l.getMedicamentId())
                            .map(Medicament::getNomCommercial)
                            .findFirst().orElse("?"))
                    .distinct()
                    .collect(java.util.stream.Collectors.joining(", "));
            lignesMedsCache.put(cmd.getId(), medsNames);
        }

        allCommandes.sort((a, b) -> {
            int ordreA = a.getStatut() == com.sgpa.model.StatutCommande.EN_COURS ? 0 : 1;
            int ordreB = b.getStatut() == com.sgpa.model.StatutCommande.EN_COURS ? 0 : 1;
            if (ordreA != ordreB) return ordreA - ordreB;
            return b.getDateCommande().compareTo(a.getDateCommande());
        });
        commandes.addAll(allCommandes);
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

    private void showAlert(String title, String content, Alert.AlertType type) {
        FXUtil.showAlert(title, content, type);
    }
}
