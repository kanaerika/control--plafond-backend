package com.afb.application.agent.port.in;

public record ModifierAgentCommande(
    long id,
    String nomComplet,
    String agence,
    String codeAgent
) {
}