package com.afb.infrastructure.agent.adapter.in.web;

import com.afb.domain.agent.exception.*;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AgentExceptionHandler {

    @ExceptionHandler(AgentNonTrouve.class)
    public ProblemDetail nonTrouve(AgentNonTrouve ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pb.setTitle("Agent introuvable");
        return pb;
    }

    @ExceptionHandler(EmailAgentDejaUtilise.class)
    public ProblemDetail emailUtilise(EmailAgentDejaUtilise ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pb.setTitle("Email déjà utilisé");
        return pb;
    }

    @ExceptionHandler(AgentHorsPartenaire.class)
    public ProblemDetail horsPartenaire(AgentHorsPartenaire ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pb.setTitle("Accès refusé");
        return pb;
    }

    @ExceptionHandler(CompteDejaActive.class)
    public ProblemDetail dejaActive(CompteDejaActive ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pb.setTitle("Compte déjà activé");
        return pb;
    }

    /**
     * Suppression refusée : l'agent porte déjà des opérations (transferts,
     * compteurs journaliers). 409 avec la marche à suivre — désactiver.
     */
    @ExceptionHandler(AgentAvecHistorique.class)
    public ProblemDetail avecHistorique(AgentAvecHistorique ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pb.setTitle("Suppression impossible");
        return pb;
    }

    /**
     * Envoi / renvoi d'invitation impossible parce que Keycloak est injoignable
     * ou pas encore configuré. On renvoie un 503 avec un message lisible, jamais
     * une trace jakarta.ws.rs.* — le front affiche directement ce message.
     */
    @ExceptionHandler(ServiceInvitationIndisponible.class)
    public ProblemDetail invitationIndisponible(ServiceInvitationIndisponible ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        pb.setTitle("Invitation non envoyée");
        return pb;
    }
}