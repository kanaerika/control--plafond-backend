package com.afb.application.transfert.port.in;

public record ExecutionCommande(
        String nomClient, String dateNaissance, String naturePiece, String numeroPiece,
        long montant, String paysDestination,
        String reference, String canal,
        Long agentId, Long partenaireId, String agenceAgent) {
}
