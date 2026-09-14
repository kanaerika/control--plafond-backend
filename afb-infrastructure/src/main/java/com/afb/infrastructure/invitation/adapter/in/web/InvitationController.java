package com.afb.infrastructure.invitation.adapter.in.web;

import com.afb.application.invitation.port.in.ActiverCompteUseCase;
import com.afb.domain.invitation.exception.InvitationInvalide;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints PUBLICS d'activation : l'invité arrive depuis son email et n'est pas
 * encore authentifié. Ouverts explicitement dans {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/api/invitation")
public class InvitationController {

    private final ActiverCompteUseCase activation;

    public InvitationController(ActiverCompteUseCase activation) {
        this.activation = activation;
    }

    /** Vérifie le lien avant d'afficher le formulaire de mot de passe. */
    @PostMapping("/validation")
    public Map<String, String> valider(@RequestParam String token) {
        return Map.of(
                "email", activation.validerLien(token),
                "message", "Lien valide. Choisissez votre mot de passe.");
    }

    @GetMapping("/{token}")
    public Map<String, String> verifier(@PathVariable String token) {
        return valider(token);
    }

    @PostMapping("/activer")
    public Map<String, String> activer(@RequestBody ActivationRequete requete) {
        return Map.of("message", activation.activer(requete.token(), requete.motDePasse()));
    }

    /**
     * 410 Gone plutôt que 404 : le lien a existé, il ne vaut simplement plus.
     * Le front distingue déjà ce code pour afficher « demandez une nouvelle
     * invitation » au lieu d'une erreur technique.
     */
    @ExceptionHandler(InvitationInvalide.class)
    public ProblemDetail invalide(InvitationInvalide ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage());
        pb.setTitle("Lien d'invitation invalide");
        return pb;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail refuse(IllegalArgumentException ex) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pb.setTitle("Mot de passe refusé");
        return pb;
    }

    public record ActivationRequete(String token, String motDePasse) {}
}
