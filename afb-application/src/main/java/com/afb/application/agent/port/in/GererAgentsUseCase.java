package com.afb.application.agent.port.in;

import java.util.List;
public interface GererAgentsUseCase {
    List<AgentResultat> lister();
    AgentResultat creer(CreerAgentCommande commande);
    AgentResultat modifier(ModifierAgentCommande commande);
    AgentResultat basculerActivation(Long id);
    String supprimer(Long id);

    /** Renvoie l'email d'invitation (« définir le mot de passe ») à l'agent. */
    String renvoyerInvitation(Long id);

    /** Déclenche pour l'agent la réinitialisation de son mot de passe (email Keycloak). */
    String reinitialiser(Long id);
}