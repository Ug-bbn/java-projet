package com.sgpa.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Lot {
    private int id;
    private int medicamentId;
    private String nomMedicament;
    private String numeroLot;
    private int quantiteStock;
    private LocalDate datePeremption;
    private BigDecimal prixAchat;

    public Lot() {
        this.prixAchat = BigDecimal.ZERO;
    }

    public Lot(int medicamentId, String numeroLot, int quantiteStock, LocalDate datePeremption, BigDecimal prixAchat) {
        this.medicamentId = medicamentId;
        this.numeroLot = numeroLot;
        this.quantiteStock = quantiteStock;
        this.datePeremption = datePeremption;
        this.prixAchat = prixAchat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMedicamentId() {
        return medicamentId;
    }

    public void setMedicamentId(int medicamentId) {
        this.medicamentId = medicamentId;
    }

    public String getNomMedicament() {
        return nomMedicament;
    }

    public void setNomMedicament(String nomMedicament) {
        this.nomMedicament = nomMedicament;
    }

    public String getNumeroLot() {
        return numeroLot;
    }

    public void setNumeroLot(String numeroLot) {
        this.numeroLot = numeroLot;
    }

    public int getQuantiteStock() {
        return quantiteStock;
    }

    public void setQuantiteStock(int quantiteStock) {
        this.quantiteStock = quantiteStock;
    }

    public LocalDate getDatePeremption() {
        return datePeremption;
    }

    public void setDatePeremption(LocalDate datePeremption) {
        this.datePeremption = datePeremption;
    }

    public BigDecimal getPrixAchat() {
        return prixAchat;
    }

    public void setPrixAchat(BigDecimal prixAchat) {
        this.prixAchat = prixAchat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lot lot = (Lot) o;
        return id == lot.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Lot %s - %s - Stock: %d", numeroLot, nomMedicament != null ? nomMedicament : "Inconnu",
                quantiteStock);
    }
}
