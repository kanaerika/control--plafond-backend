package com.afb.domain.invitation.port.out;

import com.afb.domain.invitation.model.Invitation;

import java.util.Optional;

/** PORT DE SORTIE : persistance des invitations émises. */
public interface InvitationTokenPort {

    Invitation enregistrer(Invitation invitation);

    Optional<Invitation> trouverParEmpreinte(String empreinteToken);

    /**
     * Invalide les invitations encore en attente pour cet email. Appelé avant
     * d'en émettre une nouvelle : un renvoi doit rendre l'ancien lien inopérant.
     */
    void invaliderEnAttente(String email);
}
