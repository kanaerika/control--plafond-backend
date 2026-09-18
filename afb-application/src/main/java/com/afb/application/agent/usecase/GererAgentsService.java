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

    private static final String ROLE_AGENT = Agent.ROLE_AGENT;
    private final com.afb.domain.agent.port.out.InvitationPort invitations;

    private final AgentRepositoryPort agents;
    private final AdminCourantPort adminCourant;

    public GererAgentsService(AgentRepositoryPort agents, AdminCourantPort adminCourant,
                              com.afb.domain.agent.port.out.InvitationPort invitations) {
        this.agents = agents;
        this.adminCourant = adminCourant;
        this.invitations = invitations;
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
        // Sans quoi l'adresse resterait prise dans Keycloak : recréer un agent
        // avec le même email échouerait en « email déjà utilisé ».
        invitations.supprimerCompte(agent.getEmail());
        return "Agent supprimé.";
    }

    // En écriture : l'émission d'une invitation enregistre désormais un jeton
    // (table « invitations ») en plus d'appeler le service d'authentification.
    @Override
    @Transactional
    public String renvoyerInvitation(Long id) {
        Agent agent = chargerDansMonPartenaire(id);
        invitations.renvoyerInvitation(agent.getEmail(), agent.getNomComplet(), agent.getRole());
        return "Invitation renvoyée à " + agent.getEmail() + ".";
    }

    @Override
    @Transactional
    public String reinitialiser(Long id) {
        Agent agent = chargerDansMonPartenaire(id);
        invitations.reinitialiserMotDePasse(agent.getEmail(), agent.getNomComplet(), agent.getRole());
        return "Réinitialisation du mot de passe envoyée à " + agent.getEmail() + ".";
    }
    @Override
@Transactional
public AgentResultat creer(com.afb.application.agent.port.in.CreerAgentCommande cmd) {
    String email = cmd.email().trim().toLowerCase();
    if (agents.existeParEmail(email)) {
        throw new com.afb.domain.agent.exception.EmailAgentDejaUtilise(email);
    }
    // 1. Crée l'utilisateur dans Keycloak + envoie l'invitation
    Agent nouvel = Agent.creer(cmd.nomComplet(), email, partenaireCourant(),
            cmd.agence(), cmd.codeAgent());
    invitations.creerEtInviter(nouvel, ROLE_AGENT);

    // 2. Enregistre l'agent dans la base métier (données propres à l'app)
    Agent enregistre = agents.enregistrer(nouvel);
    return versResultat(enregistre);
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