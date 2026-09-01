package com.afb.domain.transfert.model;

/** Résultat métier d'une vérification de plafond (PUR). */
public record ResultatVerification(
        boolean autorise, long plafond, long cumul, long montant,
        long restant, int pourcentageUtilise, int pourcentageApres) {

    public static ResultatVerification calculer(long cumul, long montant, long plafond) {
        long restant = Math.max(0, plafond - cumul);
        boolean autorise = montant <= restant;
        int pctUtilise = plafond <= 0 ? 0 : (int) Math.min(100, Math.round(cumul * 100.0 / plafond));
        int pctApres = plafond <= 0 ? 0 : (int) Math.min(100, Math.round((cumul + montant) * 100.0 / plafond));
        return new ResultatVerification(autorise, plafond, cumul, montant, restant, pctUtilise, pctApres);
    }
}