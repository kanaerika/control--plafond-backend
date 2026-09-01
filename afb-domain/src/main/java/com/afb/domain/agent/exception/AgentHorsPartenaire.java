package com.afb.domain.agent.exception;

public class AgentHorsPartenaire extends RuntimeException {
    public AgentHorsPartenaire() {
        super("Cet utilisateur n'appartient pas à votre institution.");
    }
}