package com.sgpa.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MedicamentTest {

    @Test
    void constructorNoArg_initializesPrixToZero() {
        Medicament m = new Medicament();
        assertEquals(BigDecimal.ZERO, m.getPrixPublic());
    }

    @Test
    void constructorFull_setsAllFields() {
        Medicament m = new Medicament("Doliprane", "Paracetamol", "Comprime",
                "500mg", new BigDecimal("3.50"), false, 10);
        assertEquals("Doliprane", m.getNomCommercial());
        assertEquals("Paracetamol", m.getPrincipeActif());
        assertEquals(new BigDecimal("3.50"), m.getPrixPublic());
        assertEquals(10, m.getSeuilMinAlerte());
        assertFalse(m.isNecessiteOrdonnance());
    }

    @Test
    void equals_sameId_areEqual() {
        Medicament m1 = new Medicament();
        m1.setId(1);
        Medicament m2 = new Medicament();
        m2.setId(1);
        assertEquals(m1, m2);
    }

    @Test
    void equals_differentId_areNotEqual() {
        Medicament m1 = new Medicament();
        m1.setId(1);
        Medicament m2 = new Medicament();
        m2.setId(2);
        assertNotEquals(m1, m2);
    }

    @Test
    void hashCode_sameId_sameHashCode() {
        Medicament m1 = new Medicament();
        m1.setId(5);
        Medicament m2 = new Medicament();
        m2.setId(5);
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    @Test
    void equals_null_returnsFalse() {
        Medicament m = new Medicament();
        m.setId(1);
        assertNotEquals(null, m);
    }

    @Test
    void toString_containsNomCommercial() {
        Medicament m = new Medicament("Doliprane", "Paracetamol", "Comprime",
                "500mg", new BigDecimal("3.50"), false, 10);
        m.setId(1);
        assertTrue(m.toString().contains("Doliprane"));
    }
}
