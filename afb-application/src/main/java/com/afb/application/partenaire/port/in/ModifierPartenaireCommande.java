package com.afb.application.partenaire.port.in;

/** Commande de modification d'un partenaire. */
public record ModifierPartenaireCommande(
        Long id,
        String nom,
        String email) {
}