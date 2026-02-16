package com.sgpa.controller;

import com.sgpa.dao.UtilisateurDAO;
import com.sgpa.dao.impl.UtilisateurDAOImpl;
import com.sgpa.model.Utilisateur;
import com.sgpa.service.AuthentificationService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

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

    private UtilisateurDAO utilisateurDAO = new UtilisateurDAOImpl();
    private AuthentificationService authService = new AuthentificationService();
    private ObservableList<Utilisateur> utilisateurs = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        // Configuration des colonnes du tableau
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colDateCreation.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        // Remplir le ComboBox des rôles
        cmbRole.setItems(FXCollections.observableArrayList("USER", "ADMIN"));
        cmbRole.setValue("USER");

        // Charger les utilisateurs
        loadUtilisateurs();

        // Sélection dans le tableau
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
        utilisateurs.addAll(utilisateurDAO.findAll());
        tableUtilisateurs.setItems(utilisateurs);
    }

    private void fillFormWithUtilisateur(Utilisateur utilisateur) {
        txtUsername.setText(utilisateur.getUsername());
        txtPassword.clear(); // Ne pas afficher le mot de passe
        txtNom.setText(utilisateur.getNom());
        txtPrenom.setText(utilisateur.getPrenom());
        cmbRole.setValue(utilisateur.getRole());
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

        if (utilisateurDAO.usernameExists(username)) {
            showError("Ce nom d'utilisateur existe déjà.");
            return;
        }

        boolean success = authService.register(username, password, nom, prenom, role);
        
        if (success) {
            showSuccess("Utilisateur créé avec succès !");
            loadUtilisateurs();
            handleClear();
        } else {
            showError("Erreur lors de la création de l'utilisateur.");
        }
    }

    @FXML
    private void handleUpdate() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez sélectionner un utilisateur à modifier.");
            return;
        }

        if (!validateForm()) {
            return;
        }

        selected.setUsername(txtUsername.getText().trim());
        selected.setNom(txtNom.getText().trim());
        selected.setPrenom(txtPrenom.getText().trim());
        selected.setRole(cmbRole.getValue());

        // Mettre à jour le mot de passe seulement s'il est renseigné
        String password = txtPassword.getText();
        if (!password.isEmpty()) {
            String hashedPassword = authService.hashPassword(password);
            selected.setPasswordHash(hashedPassword);
        }

        utilisateurDAO.update(selected);
        showSuccess("Utilisateur modifié avec succès !");
        loadUtilisateurs();
        handleClear();
    }

    @FXML
    private void handleDelete() {
        Utilisateur selected = tableUtilisateurs.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("Veuillez sélectionner un utilisateur à supprimer.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Supprimer l'utilisateur");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer l'utilisateur " + selected.getUsername() + " ?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            utilisateurDAO.delete(selected.getId());
            showSuccess("Utilisateur supprimé avec succès !");
            loadUtilisateurs();
            handleClear();
        }
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

        // Vérifier le mot de passe seulement pour la création
        if (tableUtilisateurs.getSelectionModel().getSelectedItem() == null) {
            if (txtPassword.getText().isEmpty()) {
                showError("Le mot de passe est obligatoire pour créer un utilisateur.");
                return false;
            }
            if (txtPassword.getText().length() < 4) {
                showError("Le mot de passe doit contenir au moins 4 caractères.");
                return false;
            }
        }

        return true;
    }

    private void showError(String message) {
        lblMessage.setText("❌ " + message);
        lblMessage.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        lblMessage.setVisible(true);
    }

    private void showSuccess(String message) {
        lblMessage.setText("✅ " + message);
        lblMessage.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        lblMessage.setVisible(true);
    }
}
