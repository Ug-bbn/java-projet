package com.sgpa.service;

import com.sgpa.dao.UtilisateurDAO;
import com.sgpa.model.Role;
import com.sgpa.model.Utilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthentificationServiceTest {

    @Mock
    private UtilisateurDAO utilisateurDAO;

    private AuthentificationService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthentificationService(utilisateurDAO);
    }

    @Test
    void hashPassword_returnsBCryptHash() {
        String hash = authService.hashPassword("test123");
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$"));
    }

    @Test
    void hashPassword_differentCallsProduceDifferentHashes() {
        String hash1 = authService.hashPassword("test123");
        String hash2 = authService.hashPassword("test123");
        assertNotEquals(hash1, hash2); // BCrypt with random salt
    }

    @Test
    void login_validBCryptCredentials_returnsUser() {
        String bcryptHash = authService.hashPassword("password");
        Utilisateur user = new Utilisateur("admin", bcryptHash, "Admin", "Test");
        when(utilisateurDAO.findByUsername("admin")).thenReturn(user);

        Utilisateur result = authService.login("admin", "password");

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
    }

    @Test
    void login_invalidPassword_returnsNull() {
        String bcryptHash = authService.hashPassword("correct");
        Utilisateur user = new Utilisateur("admin", bcryptHash, "Admin", "Test");
        when(utilisateurDAO.findByUsername("admin")).thenReturn(user);

        Utilisateur result = authService.login("admin", "wrong");

        assertNull(result);
    }

    @Test
    void login_unknownUser_returnsNull() {
        when(utilisateurDAO.findByUsername("inconnu")).thenReturn(null);

        Utilisateur result = authService.login("inconnu", "password");

        assertNull(result);
    }

    @Test
    void login_sha256Hash_migratedToBCrypt() {
        // SHA-256 hash of "admin"
        String sha256Hash = "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918";
        Utilisateur user = new Utilisateur("admin", sha256Hash, "Admin", "Test");
        when(utilisateurDAO.findByUsername("admin")).thenReturn(user);

        Utilisateur result = authService.login("admin", "admin");

        assertNotNull(result);
        assertTrue(result.getPasswordHash().startsWith("$2a$")); // migrated to BCrypt
        verify(utilisateurDAO).update(user); // update was called for migration
    }

    @Test
    void register_newUser_returnsTrue() {
        when(utilisateurDAO.usernameExists("newuser")).thenReturn(false);

        boolean result = authService.register("newuser", "password", "Nom", "Prenom");

        assertTrue(result);
        verify(utilisateurDAO).create(any(Utilisateur.class));
    }

    @Test
    void register_existingUser_returnsFalse() {
        when(utilisateurDAO.usernameExists("existing")).thenReturn(true);

        boolean result = authService.register("existing", "password", "Nom", "Prenom");

        assertFalse(result);
        verify(utilisateurDAO, never()).create(any());
    }

    @Test
    void isAdmin_adminRole_returnsTrue() {
        Utilisateur user = new Utilisateur();
        user.setRole(Role.ADMIN);

        assertTrue(authService.isAdmin(user));
    }

    @Test
    void isAdmin_userRole_returnsFalse() {
        Utilisateur user = new Utilisateur();
        user.setRole(Role.USER);

        assertFalse(authService.isAdmin(user));
    }

    @Test
    void isAdmin_null_returnsFalse() {
        assertFalse(authService.isAdmin(null));
    }
}
