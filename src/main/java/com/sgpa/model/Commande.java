package com.sgpa.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Commande {
    private int id;
    private int fournisseurId;
    private LocalDateTime dateCommande;
    private StatutCommande statut;
    private String numeroLot;
    private List<LigneCommande> lignes;

    public Commande() {
        this.lignes = new ArrayList<>();
        this.dateCommande = LocalDateTime.now();
        this.statut = StatutCommande.EN_COURS;
    }

    public Commande(int fournisseurId) {
        this();
        this.fournisseurId = fournisseurId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFournisseurId() { return fournisseurId; }
    public void setFournisseurId(int fournisseurId) { this.fournisseurId = fournisseurId; }

    public LocalDateTime getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }

    public StatutCommande getStatut() { return statut; }
    public void setStatut(StatutCommande statut) { this.statut = statut; }

    public String getNumeroLot() { return numeroLot; }
    public void setNumeroLot(String numeroLot) { this.numeroLot = numeroLot; }

    public List<LigneCommande> getLignes() { return lignes; }
    public void setLignes(List<LigneCommande> lignes) { this.lignes = lignes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commande commande = (Commande) o;
        return id == commande.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Commande #%d - %s - Statut: %s", id, dateCommande, statut);
    }
}
