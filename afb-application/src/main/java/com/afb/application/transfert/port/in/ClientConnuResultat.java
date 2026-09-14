package com.afb.application.transfert.port.in;

/** Client déjà connu, proposé en auto-complétion à l'agent. */
public record ClientConnuResultat(
        String nomClient,
        String dateNaissance,
        String naturePiece,
        String numeroPiece) {
}