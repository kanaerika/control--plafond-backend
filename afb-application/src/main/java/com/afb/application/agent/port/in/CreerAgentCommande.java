package com.afb.application.agent.port.in;

public record CreerAgentCommande(
        String nomComplet,
        String email,
        String agence,
        String codeAgent) {
}