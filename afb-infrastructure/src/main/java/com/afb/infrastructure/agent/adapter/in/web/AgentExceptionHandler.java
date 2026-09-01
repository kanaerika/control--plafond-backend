package com.afb.infrastructure.agent.adapter.in.web;

import com.afb.domain.agent.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
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
}