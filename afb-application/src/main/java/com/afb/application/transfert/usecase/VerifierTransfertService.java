package com.afb.application.transfert.usecase;

import com.afb.application.transfert.port.in.VerificationCommande;
import com.afb.application.transfert.port.in.VerificationResultat;
import com.afb.application.transfert.port.in.VerifierTransfertUseCase;
import com.afb.domain.transfert.model.ResultatVerification;
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

@Service
public class VerifierTransfertService implements VerifierTransfertUseCase {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TransfertRepositoryPort transferts;
    private final PlafondPort plafondPort;
    private final CompteurJournalierPort compteurs;

    public VerifierTransfertService(TransfertRepositoryPort transferts,
                                    PlafondPort plafondPort,
                                    CompteurJournalierPort compteurs) {
        this.transferts = transferts;
        this.plafondPort = plafondPort;
        this.compteurs = compteurs;
    }

    @Override
    @Transactional
    public VerificationResultat verifier(VerificationCommande cmd) {
        String nomClient = normaliser(cmd.nomClient());
        ValidationPiece.valider(cmd.naturePiece(), cmd.numeroPiece());

        long plafond = plafondPort.plafondMensuel();
        long cumul = cumulDuMois(nomClient, cmd.numeroPiece(), cmd.dateNaissance());

        ResultatVerification r = ResultatVerification.calculer(cumul, cmd.montant(), plafond);

        Long transfertId = null;
        if (!r.autorise()) {
            String motif = String.format(Locale.FRENCH,
                    "Plafond dépassé : cumul %,d + montant %,d dépasse le plafond %,d FCFA.",
                    cumul, cmd.montant(), plafond);
            enregistrer(cmd, nomClient, cumul, StatutTransfert.REFUSE_PLAFOND, motif, "R");
            if (cmd.agentId() != null) compteurs.incrementerRejetes(cmd.agentId());
        } else {
            Transfert reserve = enregistrer(cmd, nomClient, cumul, StatutTransfert.NON_CLOTURE, null, "N");
            transfertId = reserve.getId();
            if (cmd.agentId() != null) compteurs.incrementerNonClotures(cmd.agentId());
        }

        String montantFmt = String.format(Locale.FRENCH, "%,d", cmd.montant());
        String message = r.autorise()
                ? "Le transfert de " + montantFmt + " FCFA est valide. Vous pouvez exécuter cette opération."
                : "Plafond dépassé — ce client ne peut pas transférer " + montantFmt + " FCFA ce mois-ci.";

        return new VerificationResultat(r.autorise(), message, r.plafond(), r.cumul(),
                r.montant(), r.restant(), r.pourcentageUtilise(), r.pourcentageApres(), transfertId);
    }

    private Transfert enregistrer(VerificationCommande cmd, String nomClient, long cumul,
                                  StatutTransfert statut, String motif, String prefixeRef) {
        Transfert t = new Transfert();
        t.setNomClient(nomClient);
        t.setDateNaissance(cmd.dateNaissance().trim());
        t.setNaturePiece(cmd.naturePiece());
        t.setNumeroPiece(cmd.numeroPiece().trim());
        t.setMontant(cmd.montant());
        t.setPaysDestination(cmd.paysDestination());
        t.setStatut(statut);
        t.setMotif(motif);
        t.setAgence(cmd.agenceAgent());
        t.setDateTransfert(LocalDate.now(Clock.systemDefaultZone()));
        t.setCumulMois(cumul);
        t.setAgentId(cmd.agentId());
        t.setPartenaireId(cmd.partenaireId());
        t.setReferenceVerification(genererReference(prefixeRef));
        return transferts.enregistrer(t);
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

    private static String genererReference(String prefixe) {
        StringBuilder code = new StringBuilder(prefixe);
        for (int i = 0; i < 6; i++) code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return code.toString();
    }
}