package com.afb.domain.transfert.exception;

/** Levée quand un agent tente d'agir sur un transfert d'un autre partenaire. */
public class TransfertHorsPartenaire extends RuntimeException {
    public TransfertHorsPartenaire() {
        super("Ce transfert n'appartient pas à votre institution.");
    }
}