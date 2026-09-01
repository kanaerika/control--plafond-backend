package com.afb.application.agent.usecase;

import com.afb.application.agent.port.in.*;
import com.afb.domain.agent.exception.*;
import com.afb.domain.agent.model.Agent;
import com.afb.domain.agent.port.out.AdminCourantPort;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cas d'usage de gestion des agents (SOCLE).
 * RÈGLE D'OR : isolation stricte par partenaire.
 */
@Service
public class GererAgentsService implements GererAgentsUseCase {

    private static final String ROLE_AGENT = "AGENT";

    private final AgentRepositoryPort agents;
    private final AdminCourantPort adminCourant;

    public GererAgentsService(AgentRepositoryPort agents, AdminCourantPort adminCourant) {
        this.agents = agents;
        this.adminCourant = adminCourant;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AgentResultat> lister() {
        return agents.listerParPartenaire(partenaireCourant()).stream()
                .filter(Agent::estAgent)
                .map(GererAgentsService::versResultat)
                .toList();
    }

    @Override
    @Transactional
    public AgentResultat modifier(ModifierAgentCommande cmd) {
        Agent agent = chargerDansMonPartenaire(cmd.id());
        agent.modifier(cmd.nomComplet(), cmd.agence(), cmd.codeAgent());
        return versResultat(agents.enregistrer(agent));
    }

    @Override
    @Transactional
    public AgentResultat basculerActivation(Long id) {
        Agent agent = chargerDansMonPartenaire(id);
        agent.basculerActivation();
        return versResultat(agents.enregistrer(agent));
    }

    @Override
    @Transactional
    public String supprimer(Long id) {
        Agent agent = chargerDansMonPartenaire(id);
        agents.supprimer(agent);
        return "Agent supprimé.";
    }

    private Agent chargerDansMonPartenaire(Long id) {
        Agent agent = agents.trouverParId(id).orElseThrow(() -> new AgentNonTrouve(id));
        boolean memePartenaire = agent.getPartenaireId() != null
                && agent.getPartenaireId().equals(partenaireCourant());
        if (!memePartenaire || !ROLE_AGENT.equals(agent.getRole())) {
            throw new AgentHorsPartenaire();
        }
        return agent;
    }

    private Long partenaireCourant() {
        return adminCourant.partenaireIdCourant();
    }

    private static AgentResultat versResultat(Agent a) {
        return new AgentResultat(a.getId(), a.getNomComplet(), a.getEmail(), a.getRole(),
                null, a.getAgence(), a.isActif(), a.statut());
    }
}