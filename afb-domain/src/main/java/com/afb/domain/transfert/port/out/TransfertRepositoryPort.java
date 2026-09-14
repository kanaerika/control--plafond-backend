package com.afb.domain.transfert.port.out;

import com.afb.domain.transfert.model.StatutTransfert;
import com.afb.domain.transfert.model.Transfert;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransfertRepositoryPort {

    long cumulMensuel(String nomClient, String numeroPiece, String dateNaissance,
                      LocalDate debut, LocalDate fin);

    Transfert enregistrer(Transfert transfert);

    Transfert dernierTransfertDuClient(String nomClient);

    List<Transfert> rechercherClientsConnus(String recherche, Long partenaireId);

    Optional<Transfert> trouverParId(Long id);

    /** Tous les transferts d'un agent, les plus récents d'abord. */
    List<Transfert> listerParAgent(Long agentId);

    /** Tous les transferts d'un partenaire, les plus récents d'abord. */
    List<Transfert> listerParPartenaire(Long partenaireId);

    /** Transferts d'un partenaire filtrés par statut, les plus récents d'abord. */
    List<Transfert> listerParPartenaireEtStatut(Long partenaireId, StatutTransfert statut);

    /** Transferts d'un agent pour un jour donné, les plus récents d'abord. */
    List<Transfert> listerParAgentEtJour(Long agentId, java.time.LocalDate jour);
}