package com.afb.domain.partenaire.exception;

public class NomPartenaireDejaUtilise extends RuntimeException {
    public NomPartenaireDejaUtilise(String nom) {
        super("Un partenaire portant le nom « " + nom + " » existe déjà.");
    }
}