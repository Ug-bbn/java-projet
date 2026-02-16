package com.sgpa.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Medicament {
    private int id;
    private String nomCommercial;
    private String principeActif;
    private String formeGalenique;
    private String dosage;
    private BigDecimal prixPublic;
    private boolean necessiteOrdonnance;
    private int seuilMinAlerte;

    public Medicament() {
        this.prixPublic = BigDecimal.ZERO;
    }

    public Medicament(String nomCommercial, String principeActif, String formeGalenique,
            String dosage, BigDecimal prixPublic, boolean necessiteOrdonnance, int seuilMinAlerte) {
        this.nomCommercial = nomCommercial;
        this.principeActif = principeActif;
        this.formeGalenique = formeGalenique;
        this.dosage = dosage;
        this.prixPublic = prixPublic;
        this.necessiteOrdonnance = necessiteOrdonnance;
        this.seuilMinAlerte = seuilMinAlerte;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNomCommercial() {
        return nomCommercial;
    }

    public void setNomCommercial(String nomCommercial) {
        this.nomCommercial = nomCommercial;
    }

    public String getPrincipeActif() {
        return principeActif;
    }

    public void setPrincipeActif(String principeActif) {
        this.principeActif = principeActif;
    }

    public String getFormeGalenique() {
        return formeGalenique;
    }

    public void setFormeGalenique(String formeGalenique) {
        this.formeGalenique = formeGalenique;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public BigDecimal getPrixPublic() {
        return prixPublic;
    }

    public void setPrixPublic(BigDecimal prixPublic) {
        this.prixPublic = prixPublic;
    }

    public boolean isNecessiteOrdonnance() {
        return necessiteOrdonnance;
    }

    public void setNecessiteOrdonnance(boolean necessiteOrdonnance) {
        this.necessiteOrdonnance = necessiteOrdonnance;
    }

    public int getSeuilMinAlerte() {
        return seuilMinAlerte;
    }

    public void setSeuilMinAlerte(int seuilMinAlerte) {
        this.seuilMinAlerte = seuilMinAlerte;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Medicament that = (Medicament) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - %s - %s EUR %s",
                id, nomCommercial, dosage, formeGalenique, prixPublic,
                necessiteOrdonnance ? "[Ordonnance]" : "");
    }
}
