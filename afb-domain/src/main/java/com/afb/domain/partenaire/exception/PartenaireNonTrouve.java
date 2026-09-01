package com.afb.domain.partenaire.exception;

public class PartenaireNonTrouve extends RuntimeException {
    public PartenaireNonTrouve(Long id) {
        super("Partenaire introuvable (id = " + id + ").");
    }
}
