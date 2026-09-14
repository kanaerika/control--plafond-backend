package com.afb.domain.agent.exception;

/**
 * Levée quand la suppression d'un agent est refusée parce qu'il a déjà une
 * activité rattachée (transferts exécutés, compteurs journaliers). Supprimer
 * la ligne détruirait la traçabilité de ces opérations : l'agent doit être
 * désactivé, pas supprimé.
 *
 * Porte un message destiné à un administrateur métier, jamais une contrainte
 * SQL ni une trace Hibernate.
 */
public class AgentAvecHistorique extends RuntimeException {

    public AgentAvecHistorique(String nomComplet, Throwable cause) {
        super("« " + nomComplet + " » a déjà enregistré des opérations : son historique doit être "
                + "conservé, il ne peut donc pas être supprimé. Désactivez-le pour lui retirer "
                + "l'accès à la plateforme.", cause);
    }
}
