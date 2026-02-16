package com.sgpa.model;

import java.math.BigDecimal;

public class LigneVente extends AbstractLigne {
    private int venteId;
    private int lotId;

    public LigneVente() {
        super();
    }

    public LigneVente(int lotId, int quantite, BigDecimal prixUnitaire) {
        super();
        this.lotId = lotId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int getVenteId() { return venteId; }
    public void setVenteId(int venteId) { this.venteId = venteId; }

    public int getLotId() { return lotId; }
    public void setLotId(int lotId) { this.lotId = lotId; }
}
