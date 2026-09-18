package com.afb.infrastructure.securite;

import com.afb.commons.Urls;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
import com.afb.domain.partenaire.port.out.PartenaireRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuration de sécurité : le backend agit comme Resource Server OAuth2.
 * Il ne génère plus de token : il VALIDE les tokens JWT émis par Keycloak.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * S4502 (CSRF désactivé) est revue et acceptée : voir la justification au
     * niveau de {@code .csrf(...)}. La suppression vise cette seule règle — toute
     * autre alerte Sonar sur cette méthode reste signalée.
     */
    @Bean
    @SuppressWarnings("java:S4502")
    SecurityFilterChain filterChain(HttpSecurity http, AgentRepositoryPort agents,
                                    PartenaireRepositoryPort partenaires, ObjectMapper json) throws Exception {
        http
            // Après la validation du JWT : le filtre a besoin de l'identité, et
            // refuse les comptes ou partenaires désactivés depuis l'émission du jeton.
            .addFilterAfter(new CompteActifFilter(agents, partenaires, json),
                    BearerTokenAuthenticationFilter.class)
            // Reprend le bean « corsConfigurationSource » ci-dessous. Pas d'injection par
            // type : Spring MVC expose aussi un CorsConfigurationSource, d'où une ambiguïté.
            .cors(Customizer.withDefaults())
            // Revue Sonar S4502 — protection CSRF désactivée À DESSEIN, et sans risque ici :
            // le CSRF exploite un identifiant que le navigateur joint tout seul (cookie de
            // session). Cette API est STATELESS et n'accepte que le JWT porté par l'en-tête
            // Authorization, ajouté explicitement par le front : un site tiers ne peut pas
            // le forger. Aucun cookie d'authentification n'est émis ni lu.
            // Si une authentification par cookie est un jour introduite, réactiver le CSRF.
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Activation de compte : l'invité arrive depuis son email, il n'a
                // pas encore d'identifiants. Le jeton à usage unique fait foi.
                .requestMatchers("/api/invitation/**").permitAll()
                .requestMatchers("/api/v1/health", "/swagger-ui/**",
                        "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakRoleConverter()))
            );
        return http.build();
    }

    /**
     * CORS au plus juste (Sonar S5122) :
     *  - une seule origine, celle du front, lue depuis la configuration — elle
     *    n'était écrite en dur qu'en « localhost », ce qui ne vaut rien en production ;
     *  - les seuls en-têtes réellement envoyés, au lieu de « * » ;
     *  - pas de credentials : le jeton voyage dans l'en-tête Authorization, jamais
     *    dans un cookie. Autoriser les credentials n'élargissait que la surface d'attaque.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-url}") String origineFront) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(Urls.sansSlashFinal(origineFront)));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
