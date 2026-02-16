package com.sgpa.model;

import java.math.BigDecimal;
import java.util.Objects;

public abstract class AbstractLigne {
    protected int id;
    protected int quantite;
    protected BigDecimal prixUnitaire;

    public AbstractLigne() {
        this.prixUnitaire = BigDecimal.ZERO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(BigDecimal prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public BigDecimal getTotal() {
        return prixUnitaire.multiply(BigDecimal.valueOf(quantite));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractLigne that = (AbstractLigne) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
