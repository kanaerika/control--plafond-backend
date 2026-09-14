package com.afb.application.transfert.usecase;

import com.afb.application.transfert.port.in.ExecutionCommande;
import com.afb.application.transfert.port.in.ExecuterTransfertUseCase;
import com.afb.application.transfert.port.in.VerificationResultat;
import com.afb.domain.transfert.exception.StatutTransfertInvalide;
import com.afb.domain.transfert.model.StatutTransfert;
import com.afb.domain.transfert.model.Transfert;
import com.afb.domain.transfert.model.ValidationPiece;
import com.afb.domain.transfert.port.out.CompteurJournalierPort;
import com.afb.domain.transfert.port.out.PlafondPort;
import com.afb.domain.transfert.port.out.TransfertRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Exécution directe d'un transfert (fidèle à executer() de l'ancien backend, cas
 * sans réservation préalable) : valide la pièce, recontrôle le plafond, crée un
 * transfert EXECUTE et incrémente le compteur des exécutés.
 *
 * Le cas « clôture d'une réservation NON_CLOTURE » est géré par CloturerTransfertService.
 */
@Service
public class ExecuterTransfertService implements ExecuterTransfertUseCase {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TransfertRepositoryPort transferts;
    private final PlafondPort plafondPort;
    private final CompteurJournalierPort compteurs;

    public ExecuterTransfertService(TransfertRepositoryPort transferts,
                                    PlafondPort plafondPort,
                                    CompteurJournalierPort compteurs) {
        this.transferts = transferts;
        this.plafondPort = plafondPort;
        this.compteurs = compteurs;
    }

    @Override
    @Transactional
    public VerificationResultat executer(ExecutionCommande cmd) {
        String nomClient = normaliser(cmd.nomClient());
        ValidationPiece.valider(cmd.naturePiece(), cmd.numeroPiece());

        long plafond = plafondPort.plafondMensuel();
        long cumul = cumulDuMois(nomClient, cmd.numeroPiece(), cmd.dateNaissance());

        // Recontrôle du plafond avant exécution (comme l'ancien backend)
        if (cmd.montant() > plafond - cumul) {
            throw new StatutTransfertInvalide("Plafond mensuel dépassé : exécution refusée.");
        }

        Transfert t = new Transfert();
        t.setNomClient(nomClient);
        t.setDateNaissance(cmd.dateNaissance().trim());
        t.setNaturePiece(cmd.naturePiece());
        t.setNumeroPiece(cmd.numeroPiece().trim());
        t.setMontant(cmd.montant());
        t.setPaysDestination(cmd.paysDestination());
        t.setStatut(StatutTransfert.EXECUTE);
        t.setReference(cmd.reference() == null ? null : cmd.reference().trim());
        t.setCanal(cmd.canal());
        t.setAgence(cmd.agenceAgent());
        t.setDateTransfert(LocalDate.now(Clock.systemDefaultZone()));
        t.setCumulMois(cumul + cmd.montant());
        t.setAgentId(cmd.agentId());
        t.setPartenaireId(cmd.partenaireId());
        t.setReferenceVerification(genererReference());
        Transfert sauve = transferts.enregistrer(t);

        if (cmd.agentId() != null) {
            compteurs.incrementerExecutes(cmd.agentId());
        }

        String montantFmt = String.format(Locale.FRENCH, "%,d", sauve.getMontant());
        return new VerificationResultat(true,
                "Transfert de " + montantFmt + " FCFA exécuté (réf. " + sauve.getReferenceVerification() + ").",
                plafond, cumul, sauve.getMontant(), Math.max(0, plafond - (cumul + sauve.getMontant())),
                0, 0, sauve.getId());
    }

    private long cumulDuMois(String nomClient, String numeroPiece, String dateNaissance) {
        LocalDate maintenant = LocalDate.now(Clock.systemDefaultZone());
        LocalDate debut = maintenant.withDayOfMonth(1);
        LocalDate fin = maintenant.withDayOfMonth(maintenant.lengthOfMonth());
        return transferts.cumulMensuel(nomClient, numeroPiece.trim(), dateNaissance.trim(), debut, fin);
    }

    private static String normaliser(String nom) {
        return nom == null ? "" : nom.trim().replaceAll("\\s{2,}", " ");
    }

    private static String genererReference() {
        StringBuilder code = new StringBuilder("V");
        for (int i = 0; i < 6; i++) code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return code.toString();
    }
}