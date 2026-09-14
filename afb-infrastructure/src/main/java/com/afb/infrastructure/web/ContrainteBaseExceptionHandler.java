package com.afb.infrastructure.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * FILET DE SÉCURITÉ : aucune contrainte de base (clé étrangère, unicité) ne doit
 * atteindre l'écran d'un utilisateur métier sous sa forme brute. Sans ce garde-fou,
 * {@code server.error.include-message: always} renvoie tel quel le message de
 * Postgres — « viole la contrainte de clé étrangère fkb4im5rft8… » — que personne
 * hors de l'équipe technique ne peut interpréter.
 *
 * La cause exacte reste dans les logs serveur pour le diagnostic. Les cas connus
 * (par ex. la suppression d'un agent qui a un historique) sont traduits en amont
 * en exception métier et ne passent jamais par ici : ce handler ne traite que
 * l'imprévu.
 *
 * Déclaré en dernier ({@code LOWEST_PRECEDENCE}) pour ne jamais prendre la main
 * sur un handler spécifique.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
public class ContrainteBaseExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ContrainteBaseExceptionHandler.class);

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail contrainteViolee(DataIntegrityViolationException ex) {
        log.error("Contrainte de base violée — détail technique conservé côté serveur", ex);
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Cette opération est refusée car elle entrerait en conflit avec des données "
                + "déjà enregistrées. Si le problème persiste, contactez votre administrateur "
                + "technique.");
        pb.setTitle("Opération impossible");
        return pb;
    }
}
