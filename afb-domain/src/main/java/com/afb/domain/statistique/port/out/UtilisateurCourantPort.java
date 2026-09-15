package com.afb.domain.statistique.port.out;

/** Fournit le rôle et les identifiants de l'utilisateur connecté (pour scoper les stats). */
public interface UtilisateurCourantPort {
    String roleCourant();        // "SUPER_ADMIN", "ADMIN_PARTENAIRE" ou "AGENT"
    Long agentIdCourant();
    Long partenaireIdCourant();
}