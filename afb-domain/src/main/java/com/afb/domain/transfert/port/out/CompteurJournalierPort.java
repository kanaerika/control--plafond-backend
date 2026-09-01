package com.afb.domain.transfert.port.out;

public interface CompteurJournalierPort {
    void incrementerRejetes(Long agentId);
    void incrementerNonClotures(Long agentId);
    void incrementerExecutes(Long agentId);
    void incrementerAnnules(Long agentId);
}