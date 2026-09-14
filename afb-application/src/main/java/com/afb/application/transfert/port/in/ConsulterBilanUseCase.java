package com.afb.application.transfert.port.in;

public interface ConsulterBilanUseCase {
    /** Bilan du jour de l'agent connecté. */
    BilanResultat bilanDuJour();
}