package com.afb.application.invitation.usecase;

import com.afb.application.invitation.port.in.ActiverCompteUseCase;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
import com.afb.domain.agent.port.out.InvitationPort;
import com.afb.domain.invitation.exception.InvitationInvalide;
import com.afb.domain.invitation.model.Invitation;
import com.afb.domain.invitation.model.Jeton;
import com.afb.domain.invitation.port.out.InvitationTokenPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Activation d'un compte depuis le lien reçu par email.
 *
 * Le mot de passe n'est jamais stocké côté application : il est transmis à
 * Keycloak, qui reste l'unique détenteur des identifiants. Le lien est consommé
 * dans la même transaction pour qu'il ne puisse pas servir deux fois.
 */
@Service
public class ActiverCompteService implements ActiverCompteUseCase {

    private static final int LONGUEUR_MINIMALE = 8;

    private final InvitationTokenPort jetons;
    private final InvitationPort comptes;
    private final AgentRepositoryPort agents;

    public ActiverCompteService(InvitationTokenPort jetons, InvitationPort comptes,
                                AgentRepositoryPort agents) {
        this.jetons = jetons;
        this.comptes = comptes;
        this.agents = agents;
    }

    @Override
    @Transactional(readOnly = true)
    public String validerLien(String token) {
        return chargerUtilisable(token).getEmail();
    }

    @Override
    @Transactional
    public String activer(String token, String motDePasse) {
        exigerMotDePasseAcceptable(motDePasse);

        Invitation invitation = chargerUtilisable(token);
        comptes.definirMotDePasse(invitation.getEmail(), motDePasse);

        invitation.marquerUtilisee(Instant.now());
        jetons.enregistrer(invitation);
        agents.marquerInvitationAcceptee(invitation.getEmail());

        return "Votre mot de passe est enregistré. Vous pouvez maintenant vous connecter.";
    }

    private Invitation chargerUtilisable(String token) {
        if (token == null || token.isBlank()) {
            throw new InvitationInvalide();
        }
        Invitation invitation = jetons.trouverParEmpreinte(Jeton.empreinte(token))
                .orElseThrow(InvitationInvalide::new);
        if (!invitation.estUtilisable(Instant.now())) {
            throw new InvitationInvalide();
        }
        return invitation;
    }

    private static void exigerMotDePasseAcceptable(String motDePasse) {
        if (motDePasse == null || motDePasse.length() < LONGUEUR_MINIMALE) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins " + LONGUEUR_MINIMALE + " caractères.");
        }
    }
}
