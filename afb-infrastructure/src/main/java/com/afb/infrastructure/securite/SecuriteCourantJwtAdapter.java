package com.afb.infrastructure.securite;

import com.afb.domain.agent.model.Agent;
import com.afb.domain.agent.port.out.AdminCourantPort;
import com.afb.domain.agent.port.out.AgentCourantPort;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
import com.afb.domain.partenaire.model.Partenaire;
import com.afb.domain.partenaire.port.out.PartenaireRepositoryPort;
import com.afb.domain.statistique.port.out.UtilisateurCourantPort;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Résout l'utilisateur connecté depuis le JWT Keycloak (claim « email », à défaut
 * « preferred_username ») et la table locale {@code agents}. Remplace les
 * adaptateurs simulés qui renvoyaient des identifiants fixes (partenaire 1 /
 * agent 1) et faussaient tout le périmètre des listes et des statistiques.
 *
 * <p>Vocabulaire de rôle attendu par les use cases statistiques / configuration :
 * <ul>
 *   <li>{@code AGENT}            → rôle local « AGENT »</li>
 *   <li>{@code SUPER_ADMIN}      → admin dont le partenaire est Afriland First Bank</li>
 *   <li>{@code ADMIN_PARTENAIRE} → admin de tout autre partenaire</li>
 * </ul>
 * La distinction super-admin se fait sur le partenaire d'appartenance, jamais
 * sur un rôle Keycloak dédié (le realm ne connaît que ADMIN / AGENT).
 */
@Component
@Primary
public class SecuriteCourantJwtAdapter
        implements AdminCourantPort, AgentCourantPort, UtilisateurCourantPort {

    private static final String PARTENAIRE_AFRILAND = "Afriland First Bank";

    private final AgentRepositoryPort agents;
    private final PartenaireRepositoryPort partenaires;

    public SecuriteCourantJwtAdapter(AgentRepositoryPort agents,
                                     PartenaireRepositoryPort partenaires) {
        this.agents = agents;
        this.partenaires = partenaires;
    }

    @Override
    public Long agentIdCourant() {
        return agentCourant().getId();
    }

    @Override
    public Long partenaireIdCourant() {
        return agentCourant().getPartenaireId();
    }

    @Override
    public String roleCourant() {
        Agent agent = agentCourant();
        if ("AGENT".equalsIgnoreCase(agent.getRole())) {
            return "AGENT";
        }
        boolean afriland = partenaires.trouverParId(agent.getPartenaireId())
                .map(Partenaire::getNom)
                .map(nom -> nom.trim().equalsIgnoreCase(PARTENAIRE_AFRILAND))
                .orElse(false);
        return afriland ? "SUPER_ADMIN" : "ADMIN_PARTENAIRE";
    }

    private Agent agentCourant() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalStateException(
                    "Aucune authentification JWT dans le contexte de sécurité.");
        }
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = jwt.getClaimAsString("preferred_username");
        }
        final String recherche = email;
        return agents.trouverParEmail(recherche)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun agent local associé à « " + recherche + " »."));
    }
}
