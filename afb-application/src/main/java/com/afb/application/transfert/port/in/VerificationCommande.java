package com.afb.application.transfert.port.in;

public record VerificationCommande(
        String nomClient, String dateNaissance, String naturePiece, String numeroPiece,
        long montant, String paysDestination,
        Long agentId, Long partenaireId, String agenceAgent) {
}