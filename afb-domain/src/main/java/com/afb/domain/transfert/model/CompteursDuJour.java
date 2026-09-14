package com.afb.domain.transfert.model;

/** Compteurs journaliers d'un agent (valeurs lues pour le bilan). Objet pur. */
public record CompteursDuJour(
        int executes,
        int rejetes,
        int annules,
        int nonClotures) {

    public static CompteursDuJour vide() {
        return new CompteursDuJour(0, 0, 0, 0);
    }
}