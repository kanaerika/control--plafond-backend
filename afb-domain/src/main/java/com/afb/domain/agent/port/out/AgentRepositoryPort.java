package com.afb.domain.agent.port.out;

import com.afb.domain.agent.model.Agent;

import java.util.List;
import java.util.Optional;

public interface AgentRepositoryPort {
    List<Agent> listerParPartenaire(Long partenaireId);
    Optional<Agent> trouverParId(Long id);
    Optional<Agent> trouverParEmail(String email);
    boolean existeParEmail(String email);
    Agent enregistrer(Agent agent);
    void supprimer(Agent agent);

    /**
     * Bascule le compte de « Invitation en attente » à « Actif » une fois le mot
     * de passe défini depuis le lien reçu par email. Sans effet si aucun agent ne
     * porte cet email (cas d'un administrateur de partenaire).
     */
    void marquerInvitationAcceptee(String email);
}