package com.afb.domain.invitation.port.out;

/**
 * PORT DE SORTIE : envoi des emails transactionnels de la plateforme.
 *
 * L'envoi est fait par l'application elle-même (SMTP configuré côté backend),
 * et non plus délégué aux templates de Keycloak : le message est en français,
 * aux couleurs d'Afriland, et le lien pointe vers la plateforme.
 */
public interface EmailPort {

    /**
     * Envoie le lien permettant de définir son mot de passe.
     *
     * @param destinataire adresse de l'agent ou de l'administrateur invité
     * @param nomComplet   nom affiché dans le message
     * @param lien         URL complète et à usage unique vers la page d'activation
     * @param role         « ADMIN » ou « AGENT » : le message présente les
     *                     fonctionnalités auxquelles ce rôle donne accès, pour que
     *                     la personne sache ce qui l'attend avant de cliquer
     * @param renvoi       {@code true} s'il s'agit d'un renvoi (le message le précise
     *                     et signale que le lien précédent est annulé)
     */
    void envoyerInvitation(String destinataire, String nomComplet, String lien,
                           String role, boolean renvoi);
}
