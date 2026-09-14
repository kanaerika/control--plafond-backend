package com.afb.application.transfert.usecase;

import com.afb.application.transfert.port.in.ConsulterPlafondClientUseCase;
import com.afb.application.transfert.port.in.PlafondClientResultat;
import com.afb.domain.transfert.port.out.PlafondPort;
import com.afb.domain.transfert.port.out.TransfertRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Contrôle en lecture seule (aucun transfert enregistré) du cumul mensuel déjà
 * atteint par un client, pour l'aide à la saisie côté frontend.
 */
@Service
public class ConsulterPlafondClientService implements ConsulterPlafondClientUseCase {

    private final TransfertRepositoryPort transferts;
    private final PlafondPort plafondPort;

    public ConsulterPlafondClientService(TransfertRepositoryPort transferts, PlafondPort plafondPort) {
        this.transferts = transferts;
        this.plafondPort = plafondPort;
    }

    @Override
    @Transactional(readOnly = true)
    public PlafondClientResultat pour(String nomClient, String numeroPiece, String dateNaissance) {
        String nom = normaliser(nomClient);
        long plafond = plafondPort.plafondMensuel();
        long cumul = cumulDuMois(nom, numeroPiece, dateNaissance);
        return new PlafondClientResultat(cumul, plafond, cumul >= plafond);
    }

    private long cumulDuMois(String nomClient, String numeroPiece, String dateNaissance) {
        LocalDate maintenant = LocalDate.now(Clock.systemDefaultZone());
        LocalDate debut = maintenant.withDayOfMonth(1);
        LocalDate fin = maintenant.withDayOfMonth(maintenant.lengthOfMonth());
        return transferts.cumulMensuel(
                nomClient,
                numeroPiece == null ? "" : numeroPiece.trim(),
                dateNaissance == null ? "" : dateNaissance.trim(),
                debut, fin);
    }

    private static String normaliser(String nom) {
        return nom == null ? "" : nom.trim().replaceAll("\\s{2,}", " ");
    }
}
