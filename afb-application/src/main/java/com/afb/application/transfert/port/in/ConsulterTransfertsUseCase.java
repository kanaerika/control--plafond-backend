package com.afb.application.transfert.port.in;

import java.util.List;

public interface ConsulterTransfertsUseCase {

    /** Historique complet du partenaire de l'agent connecté. */
    List<TransfertResultat> historique(String recherche);

    /** Transferts non clôturés (à finaliser). */
    List<TransfertResultat> nonClotures(String recherche);

    /** Transferts exécutés (annulables). */
    List<TransfertResultat> annulables(String recherche);

    /** Détail d'un transfert (avec isolation par partenaire). */
    TransfertResultat detail(Long id);
}