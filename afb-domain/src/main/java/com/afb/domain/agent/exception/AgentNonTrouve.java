package com.afb.domain.agent.exception;

public class AgentNonTrouve extends RuntimeException {
    public AgentNonTrouve(Long id) {
        super("Agent introuvable (id = " + id + ").");
    }
}
