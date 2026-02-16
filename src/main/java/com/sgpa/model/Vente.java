package com.sgpa.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Vente {
    private int id;
    private LocalDateTime dateVente;
    private BigDecimal totalVente;
    private boolean surOrdonnance;
    private String nomsMedicaments;
    private List<LigneVente> lignes;

    public Vente() {
        this.lignes = new ArrayList<>();
        this.dateVente = LocalDateTime.now();
        this.totalVente = BigDecimal.ZERO;
    }

    public Vente(boolean surOrdonnance) {
        this();
        this.surOrdonnance = surOrdonnance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDateVente() {
        return dateVente;
    }

    public void setDateVente(LocalDateTime dateVente) {
        this.dateVente = dateVente;
    }

    public BigDecimal getTotalVente() {
        return totalVente;
    }

    public void setTotalVente(BigDecimal totalVente) {
        this.totalVente = totalVente;
    }

    public boolean isSurOrdonnance() {
        return surOrdonnance;
    }

    public void setSurOrdonnance(boolean surOrdonnance) {
        this.surOrdonnance = surOrdonnance;
    }

    public String getNomsMedicaments() {
        return nomsMedicaments;
    }

    public void setNomsMedicaments(String nomsMedicaments) {
        this.nomsMedicaments = nomsMedicaments;
    }

    public List<LigneVente> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneVente> lignes) {
        this.lignes = lignes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vente vente = (Vente) o;
        return id == vente.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Vente #%d - %s - Total: %s EUR %s - %s",
                id, dateVente, totalVente, surOrdonnance ? "[Ordonnance]" : "",
                nomsMedicaments != null ? nomsMedicaments : "");
    }
}
