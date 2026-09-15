package com.afb.domain.partenaire.exception;

/**
 * Suppression refusée : des agents de ce partenaire ont déjà enregistré des
 * opérations. Le message parle du partenaire que l'administrateur a voulu
 * supprimer, et non de l'agent sur lequel la contrainte a buté.
 *
 * Volontairement sans cause chaînée : Spring choisit le gestionnaire d'erreur
 * en parcourant aussi les causes, et celui des agents (prioritaire) aurait
 * répondu avec le message de l'agent au lieu de celui du partenaire.
 */
public class PartenaireAvecHistorique extends RuntimeException {

    public PartenaireAvecHistorique(String nom) {
        super("« " + nom + " » a déjà des transferts enregistrés : son historique doit être "
                + "conservé, il ne peut donc pas être supprimé. Désactivez-le pour couper "
                + "l'accès de ses utilisateurs à la plateforme.");
    }
}
