package com.afb.application.transfert.port.in;

public interface ExecuterTransfertUseCase {
    /** Exécute directement un transfert (crée un EXECUTE, avec contrôle du plafond). */
    VerificationResultat executer(ExecutionCommande commande);
}