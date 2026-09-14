package com.afb.infrastructure.transfert.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.afb.domain.transfert.exception.TransfertIntrouvable;
import com.afb.domain.transfert.exception.TransfertNonCloturable;
import com.afb.domain.transfert.exception.TransfertHorsPartenaire;
import com.afb.domain.transfert.exception.TransfertHorsPartenaire;
import com.afb.domain.transfert.exception.TransfertIntrouvable;
import com.afb.domain.transfert.exception.TransfertNonCloturable;
import com.afb.domain.transfert.exception.StatutTransfertInvalide;
@RestControllerAdvice(basePackages = "com.afb.infrastructure.transfert")
public class TransfertExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalide(IllegalArgumentException ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pb.setTitle("Requête invalide");
        return pb;
    }

    @ExceptionHandler(TransfertIntrouvable.class)
public ProblemDetail introuvable(TransfertIntrouvable ex) {
    ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pb.setTitle("Transfert introuvable");
    return pb;
}

@ExceptionHandler(TransfertNonCloturable.class)
public ProblemDetail nonCloturable(TransfertNonCloturable ex) {
    ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    pb.setTitle("Clôture impossible");
    return pb;
}

@ExceptionHandler(TransfertHorsPartenaire.class)
public ProblemDetail horsPartenaire(TransfertHorsPartenaire ex) {
    ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    pb.setTitle("Accès refusé");
    return pb;
}

@ExceptionHandler(StatutTransfertInvalide.class)
public ProblemDetail statutInvalide(StatutTransfertInvalide ex) {
    ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    pb.setTitle("Opération non permise");
    return pb;
}
}