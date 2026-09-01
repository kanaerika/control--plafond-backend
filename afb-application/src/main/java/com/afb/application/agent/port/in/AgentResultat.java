package com.afb.application.agent.port.in;

public record AgentResultat(
        Long id,
        String nomComplet,
        String email,
        String role,
        String partenaireNom,
        String agence,
        boolean actif,
        String statut) {
}