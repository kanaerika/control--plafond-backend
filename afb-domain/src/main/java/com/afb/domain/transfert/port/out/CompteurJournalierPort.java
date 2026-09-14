package com.afb.domain.transfert.port.out;

import com.afb.domain.transfert.model.CompteursDuJour;
import java.time.LocalDate;

public interface CompteurJournalierPort {
    void incrementerRejetes(Long agentId);
    void incrementerNonClotures(Long agentId);
    void incrementerExecutes(Long agentId);
    void incrementerAnnules(Long agentId);

    void basculerNonClotureVersExecute(Long agentId, LocalDate jour);
    void basculerNonClotureVersRejete(Long agentId, LocalDate jour);

    /** Lit les compteurs d'un agent pour un jour donné (zéros si aucun). */
    CompteursDuJour lire(Long agentId, LocalDate jour);
}