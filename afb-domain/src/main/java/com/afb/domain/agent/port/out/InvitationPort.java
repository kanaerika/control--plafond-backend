package com.afb.domain.agent.port.out;

import com.afb.domain.agent.model.Agent;

/**
 * PORT DE SORTIE : création + invitation d'un utilisateur via Keycloak.
 * Le rôle Keycloak à assigner est passé en paramètre (AGENT ou ADMIN_PARTENAIRE).
 */
public interface InvitationPort {
    /** Crée l'utilisateur dans Keycloak, lui assigne le rôle donné, (option) email,
     *  et renvoie l'identifiant Keycloak. */
    String creerEtInviter(Agent agent, String role);

    /** Émet un nouveau lien d'invitation et l'envoie par email. Le nom est fourni
     *  par l'appelant : le renvoi ne doit pas dépendre de la présence du compte
     *  dans Keycloak, sans quoi un provisionnement incomplet le rendrait
     *  impossible — précisément la situation qu'il sert à rattraper. */
    void renvoyerInvitation(String email, String nomComplet, String role);

    /** Déclenche la (ré)initialisation du mot de passe : même mécanisme, un
     *  nouveau lien à usage unique envoyé par la plateforme. */
    void reinitialiserMotDePasse(String email, String nomComplet, String role);

    /**
     * Pose définitivement le mot de passe choisi par l'invité et lève toute
     * action obligatoire restante, pour qu'il puisse se connecter directement.
     * Appelé après validation du jeton d'activation.
     */
    void definirMotDePasse(String email, String motDePasse);

    /**
     * Supprime le compte d'authentification associé à cet email. Appelé quand
     * l'agent est supprimé côté application : sinon l'adresse resterait prise
     * dans Keycloak et toute recréation échouerait en « email déjà utilisé ».
     * N'agit qu'une fois la transaction en cours validée.
     */
    void supprimerCompte(String email);
}
