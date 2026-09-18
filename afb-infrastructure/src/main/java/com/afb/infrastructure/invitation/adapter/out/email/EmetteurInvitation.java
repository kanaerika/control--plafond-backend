package com.afb.infrastructure.invitation.adapter.out.email;

import com.afb.commons.Urls;
import com.afb.domain.invitation.model.Invitation;
import com.afb.domain.invitation.model.Jeton;
import com.afb.domain.invitation.port.out.EmailPort;
import com.afb.domain.invitation.port.out.InvitationTokenPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Émet un lien d'invitation : génère le jeton, en conserve l'empreinte, puis
 * envoie l'email contenant le lien vers la page d'activation de la plateforme.
 *
 * Toute invitation encore ouverte pour la même adresse est invalidée au passage :
 * un renvoi annule le lien précédent.
 */
@Component
public class EmetteurInvitation {

    private final InvitationTokenPort jetons;
    private final EmailPort emails;
    private final String urlFrontend;

    public EmetteurInvitation(InvitationTokenPort jetons, EmailPort emails,
                              @Value("${app.frontend-url:http://localhost:4200}") String urlFrontend) {
        this.jetons = jetons;
        this.emails = emails;
        this.urlFrontend = urlFrontend;
    }

    public void emettre(String email, String nomComplet, String role, boolean renvoi) {
        jetons.invaliderEnAttente(email);

        String token = Jeton.generer();
        jetons.enregistrer(Invitation.emettre(Jeton.empreinte(token), email, Instant.now()));

        String lien = Urls.sansSlashFinal(urlFrontend)
                + "/invitation?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        emails.envoyerInvitation(email, nomComplet, lien, role, renvoi);
    }
}
