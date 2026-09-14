package com.afb.application.transfert.usecase;

import com.afb.application.transfert.port.in.ConsulterTransfertsUseCase;
import com.afb.application.transfert.port.in.TransfertResultat;
import com.afb.domain.agent.model.Agent;
import com.afb.domain.agent.port.out.AdminCourantPort;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
import com.afb.domain.transfert.exception.TransfertHorsPartenaire;
import com.afb.domain.transfert.exception.TransfertIntrouvable;
import com.afb.domain.transfert.model.StatutTransfert;
import com.afb.domain.transfert.model.Transfert;
import com.afb.domain.transfert.port.out.TransfertRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Consultation des transferts (historique, non-clôturés, annulables, détail).
 * Toutes les listes sont scopées au partenaire de l'agent connecté (isolation).
 * Le filtre porte sur la référence ou le nom du client, comme dans l'ancien backend.
 */
@Service
public class ConsulterTransfertsService implements ConsulterTransfertsUseCase {

    private final TransfertRepositoryPort transferts;
    private final AgentRepositoryPort agents;
    private final AdminCourantPort adminCourant;

    public ConsulterTransfertsService(TransfertRepositoryPort transferts,
                                      AgentRepositoryPort agents,
                                      AdminCourantPort adminCourant) {
        this.transferts = transferts;
        this.agents = agents;
        this.adminCourant = adminCourant;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransfertResultat> historique(String recherche) {
        return filtrer(transferts.listerParPartenaire(partenaireCourant()), recherche);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransfertResultat> nonClotures(String recherche) {
        return filtrer(transferts.listerParPartenaireEtStatut(
                partenaireCourant(), StatutTransfert.NON_CLOTURE), recherche);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransfertResultat> annulables(String recherche) {
        return filtrer(transferts.listerParPartenaireEtStatut(
                partenaireCourant(), StatutTransfert.EXECUTE), recherche);
    }

    @Override
    @Transactional(readOnly = true)
    public TransfertResultat detail(Long id) {
        Transfert t = transferts.trouverParId(id)
                .orElseThrow(() -> new TransfertIntrouvable(id));
        if (t.getPartenaireId() == null || !t.getPartenaireId().equals(partenaireCourant())) {
            throw new TransfertHorsPartenaire();
        }
        return versResultat(t, nomsAgents());
    }

    // ------------------------------------------------------------------

    private List<TransfertResultat> filtrer(List<Transfert> liste, String recherche) {
        String q = recherche == null ? "" : recherche.trim().toLowerCase();
        Map<Long, String> noms = nomsAgents();
        return liste.stream()
                .filter(t -> q.isEmpty()
                        || (t.getReference() != null && t.getReference().toLowerCase().contains(q))
                        || (t.getNomClient() != null && t.getNomClient().toLowerCase().contains(q)))
                .map(t -> versResultat(t, noms))
                .toList();
    }

    /** Noms des agents du partenaire courant, indexés par id (pour éviter un N+1). */
    private Map<Long, String> nomsAgents() {
        return agents.listerParPartenaire(partenaireCourant()).stream()
                .filter(a -> a.getId() != null)
                .collect(Collectors.toMap(Agent::getId, Agent::getNomComplet, (a, b) -> a));
    }

    private Long partenaireCourant() {
        return adminCourant.partenaireIdCourant();
    }

    private static TransfertResultat versResultat(Transfert t, Map<Long, String> nomsAgents) {
        String agentNom = t.getAgentId() == null ? null : nomsAgents.get(t.getAgentId());
        return new TransfertResultat(
                t.getId(), t.getNomClient(), t.getDateNaissance(), t.getNaturePiece(),
                t.getNumeroPiece(), t.getMontant(), t.getPaysDestination(),
                t.getStatut() == null ? null : t.getStatut().getLibelle(),
                t.getReference(), t.getReferenceVerification(), t.getMotif(),
                t.getAgence(), t.getCanal(), t.getDateTransfert(), t.getCumulMois(),
                t.getPartenaireId(), t.getAgentId(), agentNom);
    }
}
