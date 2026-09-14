package com.afb.application.transfert.port.in;

public interface AnnulerRejeterUseCase {

    /** Annule un transfert exécuté (EXECUTE → ANNULE). */
    TransfertResultat annuler(Long id, String motif);

    /** Rejette un transfert exécuté ou non clôturé (→ REJETE). */
    TransfertResultat rejeter(Long id, String motif);
}