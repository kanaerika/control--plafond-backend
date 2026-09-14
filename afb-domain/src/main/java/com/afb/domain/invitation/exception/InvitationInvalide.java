package com.afb.domain.invitation.exception;

/**
 * Lien d'activation inutilisable : jeton inconnu, déjà consommé, ou expiré.
 * Un seul message pour les trois cas — distinguer « inconnu » de « déjà utilisé »
 * renseignerait un attaquant sur la validité des jetons qu'il teste.
 */
public class InvitationInvalide extends RuntimeException {

    public InvitationInvalide() {
        super("Ce lien d'invitation n'est plus valable : il a peut-être déjà été utilisé "
                + "ou a expiré. Demandez une nouvelle invitation à votre administrateur.");
    }
}
