package com.afb.application.transfert.usecase;

import com.afb.application.transfert.port.in.BilanResultat;
import com.afb.application.transfert.port.in.ConsulterBilanUseCase;
import com.afb.application.transfert.port.in.TransfertResultat;
import com.afb.domain.agent.model.Agent;
import com.afb.domain.agent.port.out.AgentCourantPort;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
import com.afb.domain.transfert.model.CompteursDuJour;
import com.afb.domain.transfert.model.Transfert;
import com.afb.domain.transfert.port.out.CompteurJournalierPort;
import com.afb.domain.transfert.port.out.TransfertRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Bilan du jour de l'agent connecté. Fidèle à l'ancien backend :
 * totaux des compteurs (exécutés, rejetés, annulés, non clôturés) + liste des
 * transferts du jour de cet agent.
 */
@Service
public class ConsulterBilanService implements ConsulterBilanUseCase {

    private final TransfertRepositoryPort transferts;
    private final CompteurJournalierPort compteurs;
    private final AgentRepositoryPort agents;
    private final AgentCourantPort agentCourant;

    public ConsulterBilanService(TransfertRepositoryPort transferts,
                                 CompteurJournalierPort compteurs,
                                 AgentRepositoryPort agents,
                                 AgentCourantPort agentCourant) {
        this.transferts = transferts;
        this.compteurs = compteurs;
        this.agents = agents;
        this.agentCourant = agentCourant;
    }

    @Override
    @Transactional(readOnly = true)
    public BilanResultat bilanDuJour() {
        Long agentId = agentCourant.agentIdCourant();
        LocalDate jour = LocalDate.now(Clock.systemDefaultZone());

        CompteursDuJour c = compteurs.lire(agentId, jour);

        String nomAgent = agents.trouverParId(agentId).map(Agent::getNomComplet).orElse(null);

        List<TransfertResultat> lignes = transferts.listerParAgentEtJour(agentId, jour).stream()
                .map(t -> versResultat(t, nomAgent))
                .toList();

        return new BilanResultat(jour, c.executes(), c.rejetes(), c.annules(),
                c.nonClotures(), lignes.size(), lignes);
    }

    private static TransfertResultat versResultat(Transfert t, String nomAgent) {
        return new TransfertResultat(
                t.getId(), t.getNomClient(), t.getDateNaissance(), t.getNaturePiece(),
                t.getNumeroPiece(), t.getMontant(), t.getPaysDestination(),
                t.getStatut() == null ? null : t.getStatut().getLibelle(),
                t.getReference(), t.getReferenceVerification(), t.getMotif(),
                t.getAgence(), t.getCanal(), t.getDateTransfert(), t.getCumulMois(),
                t.getPartenaireId(), t.getAgentId(), t.getAgentId() == null ? null : nomAgent);
    }
}