package com.sgpa.model;

import java.math.BigDecimal;

public class LigneCommande extends AbstractLigne {
    private int commandeId;
    private int medicamentId;

    public LigneCommande() {
        super();
    }

    public LigneCommande(int medicamentId, int quantite, BigDecimal prixUnitaire) {
        super();
        this.medicamentId = medicamentId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public int getCommandeId() { return commandeId; }
    public void setCommandeId(int commandeId) { this.commandeId = commandeId; }

    public int getMedicamentId() { return medicamentId; }
    public void setMedicamentId(int medicamentId) { this.medicamentId = medicamentId; }
}
