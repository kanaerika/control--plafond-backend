package com.afb.infrastructure.securite;

import com.afb.domain.agent.model.Agent;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
import com.afb.domain.partenaire.model.Partenaire;
import com.afb.domain.partenaire.port.out.PartenaireRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Fait respecter la désactivation d'un agent ou d'un partenaire.
 *
 * Le jeton Keycloak reste valide après une désactivation : sans ce filtre, le
 * drapeau « actif » n'était qu'un libellé à l'écran et un compte suspendu
 * pouvait continuer à consulter et exécuter des transferts. Chaque requête
 * authentifiée est donc recoupée avec l'état courant en base.
 *
 * Volontairement non annoté {@code @Component} : Spring Boot l'enregistrerait
 * aussi comme filtre servlet global, exécuté avant que le JWT soit validé.
 * Il est branché explicitement dans {@link SecurityConfig}.
 */
public class CompteActifFilter extends OncePerRequestFilter {

    private final AgentRepositoryPort agents;
    private final PartenaireRepositoryPort partenaires;
    private final ObjectMapper json;

    public CompteActifFilter(AgentRepositoryPort agents, PartenaireRepositoryPort partenaires,
                             ObjectMapper json) {
        this.agents = agents;
        this.partenaires = partenaires;
        this.json = json;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest requete, HttpServletResponse reponse,
                                    FilterChain chaine) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            Optional<Agent> agent = agents.trouverParEmail(emailDe(jwt));
            if (agent.isPresent()) {
                String refus = motifDeRefus(agent.get());
                if (refus != null) {
                    refuser(reponse, refus);
                    return;
                }
            }
        }
        chaine.doFilter(requete, reponse);
    }

    private String motifDeRefus(Agent agent) {
        if (!agent.isActif()) {
            return "Votre compte a été désactivé. Contactez votre administrateur "
                    + "pour retrouver l'accès à la plateforme.";
        }
        if (agent.getPartenaireId() == null) {
            return null;
        }
        return partenaires.trouverParId(agent.getPartenaireId())
                .filter(p -> !p.isActif())
                .map(Partenaire::getNom)
                .map(nom -> "L'accès de « " + nom + " » à la plateforme est suspendu. "
                        + "Contactez Afriland First Bank.")
                .orElse(null);
    }

    private static String emailDe(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return (email == null || email.isBlank()) ? jwt.getClaimAsString("preferred_username") : email;
    }

    /**
     * Même forme ProblemDetail que le reste de l'API : le front lit {@code detail}.
     *
     * Rien de la requête n'est renvoyé (l'URI figurait auparavant dans « instance ») :
     * recopier une donnée entrante dans la réponse est une faille XSS réfléchie
     * (Sonar S5131). Le corps est sérialisé par Jackson plutôt que concaténé à la
     * main, et « nosniff » empêche le navigateur de l'interpréter autrement qu'en JSON.
     */
    private void refuser(HttpServletResponse reponse, String message) throws IOException {
        Map<String, Object> corps = new LinkedHashMap<>();
        corps.put("type", "about:blank");
        corps.put("title", "Compte désactivé");
        corps.put("status", HttpStatus.FORBIDDEN.value());
        corps.put("detail", message);

        reponse.setStatus(HttpStatus.FORBIDDEN.value());
        reponse.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        reponse.setCharacterEncoding(StandardCharsets.UTF_8.name());
        reponse.setHeader("X-Content-Type-Options", "nosniff");
        json.writeValue(reponse.getOutputStream(), corps);
    }
}
