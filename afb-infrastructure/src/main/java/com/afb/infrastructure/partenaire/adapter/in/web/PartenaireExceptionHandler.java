package com.afb.infrastructure.partenaire.adapter.in.web;

import com.afb.domain.partenaire.exception.AccesReserveAfriland;
import com.afb.domain.partenaire.exception.NomPartenaireDejaUtilise;
import com.afb.domain.partenaire.exception.PartenaireAvecHistorique;
import com.afb.domain.partenaire.exception.PartenaireNonTrouve;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PartenaireExceptionHandler {

    @ExceptionHandler(PartenaireNonTrouve.class)
    public ProblemDetail nonTrouve(PartenaireNonTrouve ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pb.setTitle("Partenaire introuvable");
        return pb;
    }

    @ExceptionHandler(NomPartenaireDejaUtilise.class)
    public ProblemDetail nomDejaUtilise(NomPartenaireDejaUtilise ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pb.setTitle("Nom de partenaire déjà utilisé");
        return pb;
    }

    @ExceptionHandler(AccesReserveAfriland.class)
    public ProblemDetail reserveAfriland(AccesReserveAfriland ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pb.setTitle("Accès refusé");
        return pb;
    }

    @ExceptionHandler(PartenaireAvecHistorique.class)
    public ProblemDetail avecHistorique(PartenaireAvecHistorique ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pb.setTitle("Suppression impossible");
        return pb;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail argumentInvalide(IllegalArgumentException ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pb.setTitle("Requête invalide");
        return pb;
    }
}