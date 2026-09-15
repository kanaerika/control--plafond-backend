package com.afb.domain.partenaire.exception;

/**
 * La gestion des partenaires est réservée à l'administration d'Afriland First
 * Bank. Le rôle Keycloak « ADMIN » ne suffit pas : chaque administrateur de
 * partenaire le porte aussi.
 */
public class AccesReserveAfriland extends RuntimeException {

    public AccesReserveAfriland() {
        super("La gestion des partenaires est réservée à l'administration d'Afriland First Bank.");
    }
}
