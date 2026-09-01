package com.afb.infrastructure.transfert.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.afb.infrastructure.transfert")
public class TransfertExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalide(IllegalArgumentException ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pb.setTitle("Requête invalide");
        return pb;
    }
}