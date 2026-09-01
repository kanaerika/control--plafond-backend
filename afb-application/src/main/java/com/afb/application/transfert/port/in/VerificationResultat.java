package com.afb.application.transfert.port.in;

public record VerificationResultat(
        boolean autorise, String message, long plafond, long cumul, long montant,
        long restant, int pourcentageUtilise, int pourcentageApres, Long transfertId) {
}