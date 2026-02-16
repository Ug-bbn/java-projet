package com.sgpa.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StatutCommandeTest {

    @Test
    void fromString_validEnCours_returnsEnCours() {
        assertEquals(StatutCommande.EN_COURS, StatutCommande.fromString("EN_COURS"));
    }

    @Test
    void fromString_validRecue_returnsRecue() {
        assertEquals(StatutCommande.RECUE, StatutCommande.fromString("RECUE"));
    }

    @Test
    void fromString_null_returnsEnCours() {
        assertEquals(StatutCommande.EN_COURS, StatutCommande.fromString(null));
    }

    @Test
    void fromString_invalidValue_returnsEnCours() {
        assertEquals(StatutCommande.EN_COURS, StatutCommande.fromString("ANNULEE"));
    }

    @Test
    void getLabel_returnsCorrectLabel() {
        assertEquals("EN_COURS", StatutCommande.EN_COURS.getLabel());
        assertEquals("RECUE", StatutCommande.RECUE.getLabel());
    }
}
