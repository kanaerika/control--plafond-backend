package com.afb.application.invitation.port.in;

/**
 * CAS D'USAGE : activation d'un compte depuis le lien reçu par email.
 * Endpoints publics — l'invité n'est pas encore authentifié.
 */
public interface ActiverCompteUseCase {

    /**
     * Vérifie qu'un lien est encore utilisable avant d'afficher le formulaire.
     * Lève {@code InvitationInvalide} sinon.
     *
     * @return l'email associé, pour l'afficher en lecture seule dans le formulaire
     */
    String validerLien(String token);

    /** Pose le mot de passe choisi et consomme le lien. */
    String activer(String token, String motDePasse);
}
