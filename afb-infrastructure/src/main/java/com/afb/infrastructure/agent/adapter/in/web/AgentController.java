package com.afb.infrastructure.agent.adapter.in.web;

import com.afb.application.agent.port.in.AgentResultat;
import com.afb.application.agent.port.in.GererAgentsUseCase;
import com.afb.application.agent.port.in.ModifierAgentCommande;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private final GererAgentsUseCase gererAgents;

    public AgentController(GererAgentsUseCase gererAgents) {
        this.gererAgents = gererAgents;
    }

    @GetMapping
    public List<AgentResultat> lister() {
        return gererAgents.lister();
    }

    @PutMapping("/{id}")
    public AgentResultat modifier(@PathVariable Long id,
                                  @RequestBody ModifierAgentRequete requete) {
        return gererAgents.modifier(new ModifierAgentCommande(
                id, requete.nomComplet(), requete.agence(), requete.codeAgent()));
    }

    @PatchMapping("/{id}/activation")
    public AgentResultat basculerActivation(@PathVariable Long id) {
        return gererAgents.basculerActivation(id);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> supprimer(@PathVariable Long id) {
        return Map.of("message", gererAgents.supprimer(id));
    }

    public record ModifierAgentRequete(String nomComplet, String agence, String codeAgent) {}
}