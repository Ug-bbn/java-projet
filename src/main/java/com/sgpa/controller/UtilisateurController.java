package com.sgpa.controller;

import com.sgpa.model.Role;
import com.sgpa.model.Utilisateur;
import com.sgpa.service.UtilisateurService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;

public class UtilisateurController {

    @FXML private TableView<Utilisateur> tableUtilisateurs;
    @FXML private TableColumn<Utilisateur, Integer> colId;
    @FXML private TableColumn<Utilisateur, String> colUsername;
    @FXML private TableColumn<Utilisateur, String> colNom;
    @FXML private TableColumn<Utilisateur, String> colPrenom;
    @FXML private TableColumn<Utilisateur, String> colRole;
    @FXML private TableColumn<Utilisateur, LocalDateTime> colDateCreation;

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtNom;
    @FXML private TextField txtPrenom;
    @FXML private ComboBox<String> cmbRole;
    @FXML private Label lblMessage;

    private UtilisateurService utilisateurService = new UtilisateurService();
    private ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        colUsername.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));
        colNom.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getNom()));
        colPrenom.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getPrenom()));
        colRole.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getRole() != null ? cellData.getValue().getRole().getLabel() : "USER"));
        colDateCreation.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getDateCreation()));

        cmbRole.setItems(FXCollections.observableArrayList("USER", "ADMIN"));
        cmbRole.setValue("USER");

        loadUtilisateurs();

        tableUtilisateurs.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    fillFormWithUtilisateur(newSelection);
                }
            }
        );
    }

    private void loadUtilisateurs() {
        utilisateurs.clear();
        utilisateurs.addAll(utilisateurService.getAllUtilisateurs());
        tableUtilisateurs.setItems(utilisateurs);
    }

    private void fillFormWithUtilisateur(Utilisateur utilisateur) {
        txtUsername.setText(utilisateur.getUsername());
        txtPassword.clear();
        txtNom.setText(utilisateur.getNom());
        txtPrenom.setText(utilisateur.getPrenom());
        cmbRole.setValue(utilisateur.getRole() != null ? utilisateur.getRole().getLabel() : "USER");
    }

    @FXML
    private void handleCreate() {
        if (!validateForm()) {
            return;
        }

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        String nom = txtNom.getText().trim();
        String prenom = txtPrenom.getText().trim();
        String role = cmbRole.getValue();

        if (utilisateurService.usernameExists(username)) {
            showError("Ce nom d'utilisateur existe deja.");
            return;
        }

        boolean success = utilisateurService.createUtilisateur(username, password, nom, prenom, role);

        if (success) {
            showSuccess("Utilisateur cree avec succes !");
            loadUtilisateurs();
            handleClear();
        } else {
            showError("Erreur lors de la creation de l'utilisateur.");
        }
    }

    @FXML
    private void handleUpdate() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez selectionner un utilisateur a modifier.");
            return;
        }

        if (!validateForm()) {
            return;
        }

        selected.setUsername(txtUsername.getText().trim());
        selected.setNom(txtNom.getText().trim());
        selected.setPrenom(txtPrenom.getText().trim());
        selected.setRole(Role.fromString(cmbRole.getValue()));

        String password = txtPassword.getText();
        utilisateurService.updateUtilisateur(selected, password.isEmpty() ? null : password);

        showSuccess("Utilisateur modifie avec succes !");
        loadUtilisateurs();
        handleClear();
    }

    @FXML
    private void handleDelete() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez selectionner un utilisateur a supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'utilisateur");
        alert.setContentText("Etes-vous sur de vouloir supprimer l'utilisateur " + selected.getUsername() + " ?");

        alert.showAndWait().filter(response -> response == ButtonType.OK).ifPresent(response -> {
            utilisateurService.deleteUtilisateur(selected.getId());
            showSuccess("Utilisateur supprime avec succes !");
            loadUtilisateurs();
            handleClear();
        });
    }

    @FXML
    private void handleClear() {
        txtUsername.clear();
        txtPassword.clear();
        txtNom.clear();
        txtPrenom.clear();
        cmbRole.setValue("USER");
        tableUtilisateurs.getSelectionModel().clearSelection();
        lblMessage.setVisible(false);
    }

    private boolean validateForm() {
        if (txtUsername.getText().trim().isEmpty() ||
            txtNom.getText().trim().isEmpty() ||
            txtPrenom.getText().trim().isEmpty()) {
            showError("Veuillez remplir tous les champs obligatoires.");
            return false;
        }

        if (tableUtilisateurs.getSelectionModel().getSelectedItem() == null) {
            if (txtPassword.getText().isEmpty()) {
                showError("Le mot de passe est obligatoire pour creer un utilisateur.");
                return false;
            }
            if (txtPassword.getText().length() < 4) {
                showError("Le mot de passe doit contenir au moins 4 caracteres.");
                return false;
            }
        }

        return true;
    }

    private void showError(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        lblMessage.setVisible(true);
    }

    private void showSuccess(String message) {
        lblMessage.setText(message);
        lblMessage.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        lblMessage.setVisible(true);
    }
}
