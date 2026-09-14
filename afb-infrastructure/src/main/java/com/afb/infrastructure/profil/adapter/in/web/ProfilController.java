package com.afb.infrastructure.profil.adapter.in.web;

import com.afb.domain.agent.model.Agent;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
import com.afb.domain.partenaire.model.Partenaire;
import com.afb.domain.partenaire.port.out.PartenaireRepositoryPort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * Profil de l'agent actuellement authentifié, résolu depuis le claim "email"
 * du JWT Keycloak — jamais depuis un identifiant fourni par le client.
 */
@RestController
@RequestMapping("/api/v1/profil")
public class ProfilController {

    private final AgentRepositoryPort agents;
    private final PartenaireRepositoryPort partenaires;

    public ProfilController(AgentRepositoryPort agents, PartenaireRepositoryPort partenaires) {
        this.agents = agents;
        this.partenaires = partenaires;
    }

    @GetMapping
    public ProfilResponse consulter(@AuthenticationPrincipal Jwt jwt) {
        Agent agent = trouverAgent(jwt);
        return versReponse(agent);
    }

    @PutMapping
    public ProfilResponse modifier(@AuthenticationPrincipal Jwt jwt, @RequestBody ModifierRequete r) {
        Agent agent = trouverAgent(jwt);
        Agent maj = new Agent(agent.getId(), r.nomComplet(), r.email(), agent.getRole(),
                agent.getPartenaireId(), r.agence(), agent.getCodeAgent(), agent.isActif(),
                agent.isInvitationAcceptee(), agent.isFirstLogin());
        return versReponse(agents.enregistrer(maj));
    }

    private Agent trouverAgent(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return agents.trouverParEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Aucun agent local associé à " + email));
    }

    private ProfilResponse versReponse(Agent agent) {
        String partenaireNom = partenaires.trouverParId(agent.getPartenaireId())
                .map(Partenaire::getNom)
                .orElse(null);
        return new ProfilResponse(agent.getNomComplet(), agent.getEmail(), agent.getAgence(),
                agent.getRole(), partenaireNom);
    }

    public record ModifierRequete(String nomComplet, String email, String agence) {}

    public record ProfilResponse(String nomComplet, String email, String agence, String role, String partenaireNom) {}
}
