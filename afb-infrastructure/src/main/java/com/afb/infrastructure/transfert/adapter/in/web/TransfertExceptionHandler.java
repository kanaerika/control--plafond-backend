package com.afb.infrastructure.transfert.adapter.in.web;

import com.afb.domain.transfert.exception.StatutTransfertInvalide;
import com.afb.domain.transfert.exception.TransfertHorsPartenaire;
import com.afb.domain.transfert.exception.TransfertIntrouvable;
import com.afb.domain.transfert.exception.TransfertNonCloturable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.afb.infrastructure.transfert")
public class TransfertExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalide(IllegalArgumentException ex) {
        return probleme(HttpStatus.BAD_REQUEST, "Requête invalide", ex.getMessage());
    }

    @ExceptionHandler(TransfertIntrouvable.class)
    public ProblemDetail introuvable(TransfertIntrouvable ex) {
        return probleme(HttpStatus.NOT_FOUND, "Transfert introuvable", ex.getMessage());
    }

    @ExceptionHandler(TransfertNonCloturable.class)
    public ProblemDetail nonCloturable(TransfertNonCloturable ex) {
        return probleme(HttpStatus.UNPROCESSABLE_ENTITY, "Clôture impossible", ex.getMessage());
    }

    @ExceptionHandler(TransfertHorsPartenaire.class)
    public ProblemDetail horsPartenaire(TransfertHorsPartenaire ex) {
        return probleme(HttpStatus.FORBIDDEN, "Accès refusé", ex.getMessage());
    }

    @ExceptionHandler(StatutTransfertInvalide.class)
    public ProblemDetail statutInvalide(StatutTransfertInvalide ex) {
        return probleme(HttpStatus.UNPROCESSABLE_ENTITY, "Opération non permise", ex.getMessage());
    }

    private static ProblemDetail probleme(HttpStatus statut, String titre, String detail) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(statut, detail);
        pb.setTitle(titre);
        return pb;
    }
}
