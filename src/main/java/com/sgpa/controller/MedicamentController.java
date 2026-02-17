package com.sgpa.controller;

import com.sgpa.model.Medicament;
import com.sgpa.service.MedicamentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;

public class MedicamentController {

    @FXML private TableView<Medicament> tableView;
    @FXML private TableColumn<Medicament, Integer> colId;
    @FXML private TableColumn<Medicament, String> colNom;
    @FXML private TableColumn<Medicament, String> colPrincipe;
    @FXML private TableColumn<Medicament, String> colForme;
    @FXML private TableColumn<Medicament, String> colDosage;
    @FXML private TableColumn<Medicament, Double> colPrix;
    @FXML private TableColumn<Medicament, Boolean> colOrdonnance;
    @FXML private TableColumn<Medicament, Integer> colSeuil;

    @FXML private TextField txtNom;
    @FXML private TextField txtPrincipe;
    @FXML private ComboBox<String> cmbForme;
    @FXML private TextField txtFormeAutre;
    @FXML private TextField txtDosage;
    @FXML private TextField txtPrix;
    @FXML private CheckBox chkOrdonnance;
    @FXML private TextField txtSeuil;

    private MedicamentService service = new MedicamentService();
    private ObservableList<Medicament> medicaments = FXCollections.observableArrayList();
    private Medicament selectedMedicament;
    
    // Formes galéniques courantes
    private static final ObservableList<String> FORMES_GALENIQUES = FXCollections.observableArrayList(
        "Comprimé",
        "Gélule",
        "Sirop",
        "Solution injectable",
        "Pommade",
        "Crème",
        "Suppositoire",
        "Collyre",
        "Gouttes",
        "Patch",
        "Inhalateur",
        "Autre..."
    );

    @FXML
    public void initialize() {
        // Configuration des colonnes
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nomCommercial"));
        colPrincipe.setCellValueFactory(new PropertyValueFactory<>("principeActif"));
        colForme.setCellValueFactory(new PropertyValueFactory<>("formeGalenique"));
        colDosage.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        colPrix.setCellValueFactory(new PropertyValueFactory<>("prixPublic"));
        colOrdonnance.setCellValueFactory(new PropertyValueFactory<>("necessiteOrdonnance"));
        colSeuil.setCellValueFactory(new PropertyValueFactory<>("seuilMinAlerte"));
        
        // Initialiser le ComboBox des formes
        cmbForme.setItems(FORMES_GALENIQUES);
        
        // Listener pour afficher/masquer le champ "Autre"
        cmbForme.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("Autre...".equals(newVal)) {
                txtFormeAutre.setVisible(true);
                txtFormeAutre.setManaged(true);
                txtFormeAutre.requestFocus();
            } else {
                txtFormeAutre.setVisible(false);
                txtFormeAutre.setManaged(false);
                txtFormeAutre.clear();
            }
        });

        // Listener pour la sélection
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedMedicament = newSelection;
                afficherDetails(newSelection);
            }
        });

        chargerMedicaments();
    }

    private void chargerMedicaments() {
        medicaments.clear();
        medicaments.addAll(service.getAllMedicaments());
        medicaments.sort(java.util.Comparator.comparingInt(Medicament::getId));
        tableView.setItems(medicaments);
    }

    private void afficherDetails(Medicament med) {
        txtNom.setText(med.getNomCommercial());
        txtPrincipe.setText(med.getPrincipeActif());
        
        // Gérer la forme galénique
        String forme = med.getFormeGalenique();
        if (FORMES_GALENIQUES.contains(forme)) {
            cmbForme.setValue(forme);
        } else {
            cmbForme.setValue("Autre...");
            txtFormeAutre.setText(forme);
        }
        
        txtDosage.setText(med.getDosage());
        txtPrix.setText(med.getPrixPublic() != null ? med.getPrixPublic().toPlainString() : "0");
        chkOrdonnance.setSelected(med.isNecessiteOrdonnance());
        txtSeuil.setText(String.valueOf(med.getSeuilMinAlerte()));
    }

    @FXML
    private void handleAjouter() {
        try {
            // Récupérer la forme galénique
            String forme = getFormeGalenique();
            if (forme == null || forme.isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner ou saisir une forme galénique.", Alert.AlertType.WARNING);
                return;
            }
            
            Medicament med = new Medicament(
                txtNom.getText(),
                txtPrincipe.getText(),
                forme,
                txtDosage.getText(),
                new BigDecimal(txtPrix.getText()),
                chkOrdonnance.isSelected(),
                Integer.parseInt(txtSeuil.getText())
            );
            service.ajouterMedicament(med);
            chargerMedicaments();
            viderChamps();
            showAlert("Succès", "Médicament ajouté avec succès !", Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs valides pour le prix et le seuil.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleModifier() {
        if (selectedMedicament == null) {
            showAlert("Erreur", "Veuillez sélectionner un médicament à modifier.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Récupérer la forme galénique
            String forme = getFormeGalenique();
            if (forme == null || forme.isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner ou saisir une forme galénique.", Alert.AlertType.WARNING);
                return;
            }
            
            selectedMedicament.setNomCommercial(txtNom.getText());
            selectedMedicament.setPrincipeActif(txtPrincipe.getText());
            selectedMedicament.setFormeGalenique(forme);
            selectedMedicament.setDosage(txtDosage.getText());
            selectedMedicament.setPrixPublic(new BigDecimal(txtPrix.getText()));
            selectedMedicament.setNecessiteOrdonnance(chkOrdonnance.isSelected());
            selectedMedicament.setSeuilMinAlerte(Integer.parseInt(txtSeuil.getText()));

            service.modifierMedicament(selectedMedicament);
            int selectedId = selectedMedicament.getId();
            chargerMedicaments();
            // Re-sélectionner le médicament modifié
            medicaments.stream()
                    .filter(m -> m.getId() == selectedId)
                    .findFirst()
                    .ifPresent(m -> tableView.getSelectionModel().select(m));
            showAlert("Succès", "Médicament modifié avec succès !", Alert.AlertType.INFORMATION);
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs valides.", Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSupprimer() {
        if (selectedMedicament == null) {
            showAlert("Erreur", "Veuillez sélectionner un médicament à supprimer.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation");
        confirmation.setHeaderText("Supprimer le médicament ?");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer " + selectedMedicament.getNomCommercial() + " ?");

        confirmation.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
            service.supprimerMedicament(selectedMedicament.getId());
            chargerMedicaments();
            viderChamps();
            showAlert("Succes", "Medicament supprime avec succes !", Alert.AlertType.INFORMATION);
        });
    }

    @FXML
    private void handleNouveau() {
        viderChamps();
        selectedMedicament = null;
    }

    private void viderChamps() {
        txtNom.clear();
        txtPrincipe.clear();
        cmbForme.setValue(null);
        txtFormeAutre.clear();
        txtFormeAutre.setVisible(false);
        txtFormeAutre.setManaged(false);
        txtDosage.clear();
        txtPrix.clear();
        chkOrdonnance.setSelected(false);
        txtSeuil.clear();
    }
    
    /**
     * Récupère la forme galénique sélectionnée ou saisie
     */
    private String getFormeGalenique() {
        String forme = cmbForme.getValue();
        if ("Autre...".equals(forme)) {
            return txtFormeAutre.getText().trim();
        }
        return forme;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
