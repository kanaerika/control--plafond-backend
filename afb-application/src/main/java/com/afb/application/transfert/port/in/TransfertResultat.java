package com.afb.application.transfert.port.in;

import java.time.LocalDate;

/** Représentation d'un transfert exposée aux adaptateurs d'entrée (web). */
public record TransfertResultat(
        Long id,
        String nomClient,
        String dateNaissance,
        String naturePiece,
        String numeroPiece,
        long montant,
        String paysDestination,
        String statut,
        String reference,
        String referenceVerification,
        String motif,
        String agence,
        String canal,
        LocalDate dateTransfert,
        long cumulMois,
        Long partenaireId,
        Long agentId,
        String agentNom
) {}
