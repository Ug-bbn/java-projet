package com.sgpa.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class LigneCommandeTest {

    @Test
    void getTotal_calculatesCorrectly() {
        LigneCommande ligne = new LigneCommande(1, 5, new BigDecimal("10.00"));
        assertEquals(new BigDecimal("50.00"), ligne.getTotal());
    }

    @Test
    void getTotal_zeroQuantity_returnsZero() {
        LigneCommande ligne = new LigneCommande(1, 0, new BigDecimal("10.00"));
        assertEquals(new BigDecimal("0.00"), ligne.getTotal());
    }

    @Test
    void constructorNoArg_initializesPrixToZero() {
        LigneCommande ligne = new LigneCommande();
        assertEquals(BigDecimal.ZERO, ligne.getPrixUnitaire());
    }

    @Test
    void inheritsEqualsFromAbstractLigne() {
        LigneCommande l1 = new LigneCommande();
        l1.setId(1);
        LigneCommande l2 = new LigneCommande();
        l2.setId(1);
        assertEquals(l1, l2);
    }

    @Test
    void ligneVente_getTotal_calculatesCorrectly() {
        LigneVente ligne = new LigneVente(1, 3, new BigDecimal("15.50"));
        assertEquals(new BigDecimal("46.50"), ligne.getTotal());
    }
}
