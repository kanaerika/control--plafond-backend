package com.afb.infrastructure.referentiel.adapter.in.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Données de référence utilisées par les formulaires du frontend
 * (natures de pièce, pays, rôles, canaux, plafond configuré).
 * Statique pour l'instant : à faire évoluer vers une source configurable
 * si ces listes doivent devenir administrables sans redéploiement.
 */
@RestController
@RequestMapping("/api/v1/referentiel")
public class ReferentielController {

    @Value("${app.plafond-mensuel}")
    private long plafondMensuel;

    private static final List<String> NATURES_PIECE = List.of(
            "Carte Nationale d'Identité", "Passeport", "Permis de conduire", "Carte consulaire"
    );

    private static final List<String> PAYS_HORS_CEMAC = List.of(
            "France", "Belgique", "Etats-Unis", "Canada", "Chine", "Allemagne",
            "Espagne", "Italie", "Royaume-Uni", "Côte d'Ivoire", "Sénégal", "Mali", "Maroc"
    );

    private static final List<String> ROLES = List.of("ADMIN", "AGENT");

    private static final List<Canal> CANAUX = List.of(
            new Canal("Espèces", "Retrait en espèces au guichet"),
            new Canal("Virement bancaire", "Virement sur compte bancaire"),
            new Canal("Mobile Money", "Transfert vers portefeuille mobile")
    );

    @GetMapping
    public ReferentielResponse obtenir() {
        return new ReferentielResponse(NATURES_PIECE, PAYS_HORS_CEMAC, ROLES, CANAUX, plafondMensuel);
    }

    public record Canal(String nom, String description) {}

    public record ReferentielResponse(
            List<String> naturesPiece,
            List<String> pays,
            List<String> roles,
            List<Canal> canaux,
            long plafond
    ) {}
}
