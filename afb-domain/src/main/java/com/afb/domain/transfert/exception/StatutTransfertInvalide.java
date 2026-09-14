package com.afb.domain.transfert.exception;

/** Levée quand une opération n'est pas permise pour le statut actuel du transfert. */
public class StatutTransfertInvalide extends RuntimeException {
    public StatutTransfertInvalide(String message) {
        super(message);
    }
}