package com.afb.application.partenaire.port.in;

public record CreerPartenaireCommande(
        String nom,
        String email,
        String nomAdministrateur) {
}