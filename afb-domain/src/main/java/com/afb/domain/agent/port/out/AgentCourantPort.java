package com.afb.domain.agent.port.out;

/**
 * PORT DE SORTIE : fournit l'identité de l'agent connecté (son id).
 * Aujourd'hui simulé ; branché sur le JWT plus tard.
 */
public interface AgentCourantPort {
    Long agentIdCourant();
}