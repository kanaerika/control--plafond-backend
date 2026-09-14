package com.afb.application.transfert.port.in;

/**
 * Contrôle en lecture seule du plafond mensuel déjà atteint par un client
 * (sans enregistrement d'un transfert).
 *
 * @param cumul   montant cumulé des transferts exécutés du client ce mois-ci
 * @param plafond plafond mensuel configuré
 * @param depasse vrai si le cumul atteint ou dépasse déjà le plafond
 */
public record PlafondClientResultat(long cumul, long plafond, boolean depasse) {
}
