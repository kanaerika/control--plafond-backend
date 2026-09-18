package com.afb.domain.transfert.model;

/** Règle métier : un motif d'annulation/rejet est obligatoire (min. 10 caractères). */
public final class ValidationMotif {

    private ValidationMotif() {
        // Classe utilitaire à méthodes statiques : elle ne doit pas être instanciée.
    }

    public static void valider(String motif) {
        if (motif == null || motif.trim().length() < 10) {
            throw new IllegalArgumentException(
                    "Le motif est obligatoire et doit contenir au moins 10 caractères.");
        }
    }
}