package com.afb.application.partenaire.port.in;

/**
 * Résultat applicatif décrivant un partenaire pour l'extérieur.
 *
 * Distinct de l'objet domaine (Partenaire) ET de tout DTO web : l'application
 * expose ses propres objets de résultat, sans dépendre de la couche web.
 * L'adaptateur REST le traduira ensuite en réponse HTTP/JSON.
 */
public record PartenaireResultat(
        Long id,
        String nom,
        String email,
        boolean actif,
        String statutAdministrateur) {
}
