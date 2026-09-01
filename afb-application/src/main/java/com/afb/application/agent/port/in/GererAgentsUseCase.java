package com.afb.application.agent.port.in;

import java.util.List;
public interface GererAgentsUseCase {
    List<AgentResultat> lister();
    AgentResultat modifier(ModifierAgentCommande commande);
    AgentResultat basculerActivation(Long id);
    String supprimer(Long id);
}