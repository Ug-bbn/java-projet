package com.sgpa.service;

import com.sgpa.dao.LotDAO;
import com.sgpa.dao.MedicamentDAO;
import com.sgpa.model.Medicament;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicamentServiceTest {

    @Mock
    private MedicamentDAO medicamentDAO;
    @Mock
    private LotDAO lotDAO;

    private MedicamentService service;

    @BeforeEach
    void setUp() {
        service = new MedicamentService(medicamentDAO, lotDAO);
    }

    @Test
    void ajouterMedicament_validData_callsCreate() {
        Medicament med = new Medicament("Doliprane", "Paracetamol", "Comprime",
                "500mg", new BigDecimal("3.50"), false, 10);

        service.ajouterMedicament(med);

        verify(medicamentDAO).create(med);
    }

    @Test
    void ajouterMedicament_nullName_throwsException() {
        Medicament med = new Medicament(null, "Paracetamol", "Comprime",
                "500mg", new BigDecimal("3.50"), false, 10);

        assertThrows(IllegalArgumentException.class, () -> service.ajouterMedicament(med));
        verify(medicamentDAO, never()).create(any());
    }

    @Test
    void ajouterMedicament_emptyName_throwsException() {
        Medicament med = new Medicament("  ", "Paracetamol", "Comprime",
                "500mg", new BigDecimal("3.50"), false, 10);

        assertThrows(IllegalArgumentException.class, () -> service.ajouterMedicament(med));
    }

    @Test
    void ajouterMedicament_negativePrix_throwsException() {
        Medicament med = new Medicament("Doliprane", "Paracetamol", "Comprime",
                "500mg", new BigDecimal("-1.00"), false, 10);

        assertThrows(IllegalArgumentException.class, () -> service.ajouterMedicament(med));
    }

    @Test
    void ajouterMedicament_negativeSeuil_throwsException() {
        Medicament med = new Medicament("Doliprane", "Paracetamol", "Comprime",
                "500mg", new BigDecimal("3.50"), false, -5);

        assertThrows(IllegalArgumentException.class, () -> service.ajouterMedicament(med));
    }

    @Test
    void getMedicamentsEnAlerteStock_returnsLowStockItems() {
        Medicament m1 = new Medicament("Med1", "P1", "C", "D", new BigDecimal("5"), false, 10);
        m1.setId(1);
        Medicament m2 = new Medicament("Med2", "P2", "C", "D", new BigDecimal("5"), false, 5);
        m2.setId(2);

        when(medicamentDAO.findAll()).thenReturn(Arrays.asList(m1, m2));
        when(lotDAO.getStockTotal(1)).thenReturn(3);  // below threshold of 10
        when(lotDAO.getStockTotal(2)).thenReturn(20); // above threshold of 5

        List<Medicament> alertes = service.getMedicamentsEnAlerteStock();

        assertEquals(1, alertes.size());
        assertEquals("Med1", alertes.get(0).getNomCommercial());
    }

    @Test
    void getAllMedicaments_delegatesToDAO() {
        Medicament m = new Medicament();
        when(medicamentDAO.findAll()).thenReturn(List.of(m));

        List<Medicament> result = service.getAllMedicaments();

        assertEquals(1, result.size());
        verify(medicamentDAO).findAll();
    }
}
