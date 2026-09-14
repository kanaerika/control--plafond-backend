package com.afb.domain.transfert.exception;

/** Levée quand on tente de clôturer un transfert qui n'est pas en statut NON_CLOTURE. */
public class TransfertNonCloturable extends RuntimeException {
    public TransfertNonCloturable() {
        super("Seul un transfert non clôturé peut être clôturé.");
    }
}