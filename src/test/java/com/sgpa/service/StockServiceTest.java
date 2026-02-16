package com.sgpa.service;

import com.sgpa.dao.LotDAO;
import com.sgpa.model.Lot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private LotDAO lotDAO;

    private StockService service;

    @BeforeEach
    void setUp() {
        service = new StockService(lotDAO);
    }

    @Test
    void ajouterLot_validData_callsCreate() {
        Lot lot = new Lot(1, "LOT-001", 100, LocalDate.now().plusMonths(6), new BigDecimal("5.00"));

        service.ajouterLot(lot);

        verify(lotDAO).create(lot);
    }

    @Test
    void ajouterLot_negativeQuantite_throwsException() {
        Lot lot = new Lot(1, "LOT-001", -10, LocalDate.now().plusMonths(6), new BigDecimal("5.00"));

        assertThrows(IllegalArgumentException.class, () -> service.ajouterLot(lot));
        verify(lotDAO, never()).create(any());
    }

    @Test
    void ajouterLot_negativePrix_throwsException() {
        Lot lot = new Lot(1, "LOT-001", 100, LocalDate.now().plusMonths(6), new BigDecimal("-5.00"));

        assertThrows(IllegalArgumentException.class, () -> service.ajouterLot(lot));
    }

    @Test
    void ajouterLot_pastDate_throwsException() {
        Lot lot = new Lot(1, "LOT-001", 100, LocalDate.now().minusDays(1), new BigDecimal("5.00"));

        assertThrows(IllegalArgumentException.class, () -> service.ajouterLot(lot));
    }

    @Test
    void supprimerLot_callsDelete() {
        service.supprimerLot(5);
        verify(lotDAO).delete(5);
    }
}
