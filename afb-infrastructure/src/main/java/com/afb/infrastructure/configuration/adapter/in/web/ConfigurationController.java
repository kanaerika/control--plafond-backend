package com.afb.infrastructure.configuration.adapter.in.web;

import com.afb.application.configuration.port.in.ConfigurationResultat;
import com.afb.application.configuration.port.in.GererConfigurationUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/configuration")
public class ConfigurationController {

    private final GererConfigurationUseCase configuration;

    public ConfigurationController(GererConfigurationUseCase configuration) {
        this.configuration = configuration;
    }

    /** Tout le monde connecté peut lire le plafond. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ConfigurationResultat lire() {
        return configuration.lire();
    }

    /**
     * Réservé à l'admin Afriland : le rôle Keycloak « ADMIN » ouvre l'accès,
     * puis {@code GererConfigurationService} vérifie que le partenaire courant
     * est bien Afriland (rôle métier SUPER_ADMIN) avant d'appliquer le changement.
     */
    @PutMapping("/plafond")
    @PreAuthorize("hasRole('ADMIN')")
    public ConfigurationResultat modifierPlafond(@RequestBody ModifierPlafondRequete r) {
        return configuration.modifierPlafond(r.plafondMensuel());
    }

    @ExceptionHandler(SecurityException.class)
    public ProblemDetail refuse(SecurityException ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pb.setTitle("Accès refusé");
        return pb;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalide(IllegalArgumentException ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pb.setTitle("Requête invalide");
        return pb;
    }

    public record ModifierPlafondRequete(long plafondMensuel) {}
}