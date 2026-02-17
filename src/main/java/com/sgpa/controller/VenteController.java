package com.sgpa.controller;

import com.sgpa.model.Medicament;
import com.sgpa.model.Vente;
import com.sgpa.service.MedicamentService;
import com.sgpa.service.VenteService;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class VenteController {

    // --- Article form ---
    @FXML
    private ComboBox<Medicament> cmbMedicament;
    @FXML
    private TextField txtQuantite;
    @FXML
    private Label lblStockDisponible;
    @FXML
    private CheckBox chkOrdonnance;

    // --- Panier table ---
    @FXML
    private TableView<ArticlePanier> tablePanier;
    @FXML
    private TableColumn<ArticlePanier, String> colPanierMedicament;
    @FXML
    private TableColumn<ArticlePanier, Integer> colPanierQuantite;
    @FXML
    private TableColumn<ArticlePanier, String> colPanierPrixUnit;
    @FXML
    private TableColumn<ArticlePanier, String> colPanierTotal;
    @FXML
    private Label lblTotalVente;

    // --- Historique table ---
    @FXML
    private TableView<Vente> tableHistorique;
    @FXML
    private TableColumn<Vente, Integer> colId;
    @FXML
    private TableColumn<Vente, String> colMedicaments;
    @FXML
    private TableColumn<Vente, String> colDate;
    @FXML
    private TableColumn<Vente, Double> colTotal;
    @FXML
    private TableColumn<Vente, Boolean> colOrdonnance;

    private final MedicamentService medicamentService = new MedicamentService();
    private final VenteService venteService = new VenteService();
    private final ObservableList<Vente> ventes = FXCollections.observableArrayList();
    private final ObservableList<ArticlePanier> panier = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Load medicaments
        ObservableList<Medicament> medicaments = FXCollections.observableArrayList(
                medicamentService.getAllMedicaments());
        cmbMedicament.setItems(medicaments);

        cmbMedicament.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Medicament item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getNomCommercial() + " - " + item.getPrixPublic() + " \u20ac");
            }
        });
        cmbMedicament.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Medicament item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null
                        : item.getNomCommercial() + " - " + item.getPrixPublic() + " \u20ac");
            }
        });

        cmbMedicament.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                int stock = medicamentService.getStockTotal(newVal.getId());
                lblStockDisponible.setText("Stock disponible : " + stock);
            } else {
                lblStockDisponible.setText("");
            }
        });

        // --- Panier table columns ---
        colPanierMedicament.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().nomMedicament));
        colPanierQuantite.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleIntegerProperty(cd.getValue().quantite).asObject());
        colPanierPrixUnit.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().prixUnitaire.toPlainString() + " \u20ac"));
        colPanierPrixUnit.setStyle("-fx-alignment: CENTER-RIGHT;");
        colPanierTotal.setCellValueFactory(cd ->
                new javafx.beans.property.SimpleStringProperty(cd.getValue().getTotal().toPlainString() + " \u20ac"));
        colPanierTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        tablePanier.setItems(panier);
        panier.addListener((ListChangeListener<ArticlePanier>) c -> updateTotalLabel());
        updateTotalLabel();

        // --- Historique table columns ---
        colId.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getId()).asObject());
        colMedicaments.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getNomsMedicaments()));
        colDate.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(
                cd.getValue().getDateVente().toString()));
        colTotal.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleDoubleProperty(
                        cd.getValue().getTotalVente() != null ? cd.getValue().getTotalVente().doubleValue() : 0)
                        .asObject());
        colOrdonnance.setCellValueFactory(
                cd -> new javafx.beans.property.SimpleBooleanProperty(cd.getValue().isSurOrdonnance())
                        .asObject());

        chargerHistorique();
    }

    @FXML
    private void handleAjouterArticle() {
        Medicament medicament = cmbMedicament.getValue();
        if (medicament == null) {
            showAlert("Erreur", "Veuillez s\u00e9lectionner un m\u00e9dicament.", Alert.AlertType.WARNING);
            return;
        }

        int quantite;
        try {
            quantite = Integer.parseInt(txtQuantite.getText().trim());
            if (quantite <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Erreur", "La quantit\u00e9 doit \u00eatre un entier positif.", Alert.AlertType.WARNING);
            return;
        }

        int stock = medicamentService.getStockTotal(medicament.getId());
        // Sum already in panier for this medicament
        int dejaDansPanier = panier.stream()
                .filter(a -> a.medicamentId == medicament.getId())
                .mapToInt(a -> a.quantite)
                .sum();
        if (dejaDansPanier + quantite > stock) {
            showAlert("Erreur", "Stock insuffisant. Disponible: " + (stock - dejaDansPanier), Alert.AlertType.WARNING);
            return;
        }

        panier.add(new ArticlePanier(medicament.getId(), medicament.getNomCommercial(),
                quantite, medicament.getPrixPublic()));
        txtQuantite.clear();
        cmbMedicament.getSelectionModel().clearSelection();
        cmbMedicament.requestFocus();
    }

    @FXML
    private void handleRetirerArticle() {
        ArticlePanier selected = tablePanier.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Erreur", "Veuillez s\u00e9lectionner un article \u00e0 retirer.", Alert.AlertType.WARNING);
            return;
        }
        panier.remove(selected);
    }

    @FXML
    private void handleEnregistrerVente() {
        if (panier.isEmpty()) {
            showAlert("Erreur", "Le panier est vide. Ajoutez au moins un m\u00e9dicament.", Alert.AlertType.WARNING);
            return;
        }

        boolean surOrdonnance = chkOrdonnance.isSelected();

        // Build articles map (aggregate same medicament)
        Map<Integer, Integer> articles = new LinkedHashMap<>();
        for (ArticlePanier a : panier) {
            articles.merge(a.medicamentId, a.quantite, Integer::sum);
        }

        boolean success = venteService.enregistrerVenteMulti(articles, surOrdonnance);

        if (success) {
            showAlert("Succ\u00e8s", "Vente enregistr\u00e9e avec succ\u00e8s !", Alert.AlertType.INFORMATION);
            panier.clear();
            chkOrdonnance.setSelected(false);
            chargerHistorique();
        } else {
            showAlert("Erreur", "Erreur lors de l'enregistrement. V\u00e9rifiez le stock.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleNouveau() {
        panier.clear();
        cmbMedicament.setValue(null);
        txtQuantite.clear();
        chkOrdonnance.setSelected(false);
        lblStockDisponible.setText("");
    }

    private void chargerHistorique() {
        ventes.clear();
        ventes.addAll(venteService.getHistoriqueVentes());
        tableHistorique.setItems(ventes);
    }

    private void updateTotalLabel() {
        BigDecimal total = panier.stream()
                .map(ArticlePanier::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotalVente.setText(panier.isEmpty() ? ""
                : String.format("Total : %s \u20ac  (%d article%s)",
                        total.toPlainString(), panier.size(), panier.size() > 1 ? "s" : ""));
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Inner class for panier items
    public static class ArticlePanier {
        final int medicamentId;
        final String nomMedicament;
        final int quantite;
        final BigDecimal prixUnitaire;

        public ArticlePanier(int medicamentId, String nomMedicament, int quantite, BigDecimal prixUnitaire) {
            this.medicamentId = medicamentId;
            this.nomMedicament = nomMedicament;
            this.quantite = quantite;
            this.prixUnitaire = prixUnitaire;
        }

        public BigDecimal getTotal() {
            return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
        }
    }
}
