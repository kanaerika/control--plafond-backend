package com.afb.domain.transfert.exception;

/** Levée quand un transfert recherché n'existe pas. */
public class TransfertIntrouvable extends RuntimeException {
    public TransfertIntrouvable(Long id) {
        super("Transfert introuvable (id = " + id + ").");
    }
}