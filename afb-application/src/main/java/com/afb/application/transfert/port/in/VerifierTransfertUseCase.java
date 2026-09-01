package com.afb.application.transfert.port.in;

public interface VerifierTransfertUseCase {
    VerificationResultat verifier(VerificationCommande commande);
}