package com.afb.infrastructure.agent.adapter.in.web;

import com.afb.application.agent.port.in.AgentResultat;
import com.afb.application.agent.port.in.GererAgentsUseCase;
import com.afb.application.agent.port.in.ModifierAgentCommande;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Gestion des agents : réservée aux administrateurs. Sans cette restriction, un
 * agent pouvait créer, suspendre ou supprimer ses collègues — le service ne
 * vérifie que l'appartenance au même partenaire, pas le rôle de l'appelant.
 */
@RestController
@RequestMapping("/api/v1/agents")
@PreAuthorize("hasRole('ADMIN')")
public class AgentController {

    /** Clé de la réponse JSON attendue par le front pour les actions sans corps. */
    private static final String CLE_MESSAGE = "message";

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
        return Map.of(CLE_MESSAGE, gererAgents.supprimer(id));
    }

    @PostMapping("/{id}/invitation")
    public Map<String, String> renvoyerInvitation(@PathVariable Long id) {
        return Map.of(CLE_MESSAGE, gererAgents.renvoyerInvitation(id));
    }

    @PostMapping("/{id}/reinitialisation")
    public Map<String, String> reinitialiser(@PathVariable Long id) {
        return Map.of(CLE_MESSAGE, gererAgents.reinitialiser(id));
    }

    @PostMapping
public AgentResultat creer(@RequestBody CreerAgentRequete r) {
    return gererAgents.creer(new com.afb.application.agent.port.in.CreerAgentCommande(
            r.nomComplet(), r.email(), r.agence(), r.codeAgent()));
}
    public record CreerAgentRequete(String nomComplet, String email, String agence, String codeAgent) {}
    public record ModifierAgentRequete(String nomComplet, String agence, String codeAgent) {}
}