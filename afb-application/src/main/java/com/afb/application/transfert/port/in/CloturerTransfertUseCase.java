package com.afb.application.transfert.port.in;

public interface CloturerTransfertUseCase {
    /** Clôture un transfert réservé (NON_CLOTURE) en saisissant la référence de la plateforme. */
    VerificationResultat cloturer(CloturerCommande commande);
}