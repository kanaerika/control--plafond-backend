package com.afb.application.transfert.usecase;

import com.afb.application.transfert.port.in.CloturerCommande;
import com.afb.application.transfert.port.in.CloturerTransfertUseCase;
import com.afb.application.transfert.port.in.VerificationResultat;
import com.afb.domain.agent.port.out.AdminCourantPort;
import com.afb.domain.transfert.exception.TransfertHorsPartenaire;
import com.afb.domain.transfert.exception.TransfertIntrouvable;
import com.afb.domain.transfert.exception.TransfertNonCloturable;
import com.afb.domain.transfert.model.StatutTransfert;
import com.afb.domain.transfert.model.Transfert;
import com.afb.domain.transfert.port.out.CompteurJournalierPort;
import com.afb.domain.transfert.port.out.TransfertRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

/**
 * Clôture différée d'un transfert resté NON_CLOTURE : l'agent saisit la référence
 * de la plateforme externe. Fidèle à l'ancien cloturer() :
 *  - isolation par partenaire
 *  - seul un NON_CLOTURE peut être clôturé
 *  - passe en EXECUTE, met à jour le cumul et la référence
 *  - ajuste les compteurs du jour d'origine (non clôturé -1, exécuté +1)
 */
@Service
public class CloturerTransfertService implements CloturerTransfertUseCase {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final TransfertRepositoryPort transferts;
    private final CompteurJournalierPort compteurs;
    private final AdminCourantPort adminCourant;

    public CloturerTransfertService(TransfertRepositoryPort transferts,
                                    CompteurJournalierPort compteurs,
                                    AdminCourantPort adminCourant) {
        this.transferts = transferts;
        this.compteurs = compteurs;
        this.adminCourant = adminCourant;
    }

    @Override
    @Transactional
    public VerificationResultat cloturer(CloturerCommande cmd) {
        Transfert t = transferts.trouverParId(cmd.transfertId())
                .orElseThrow(() -> new TransfertIntrouvable(cmd.transfertId()));

        // Isolation par partenaire
        Long partenaireCourant = adminCourant.partenaireIdCourant();
        if (t.getPartenaireId() == null || !t.getPartenaireId().equals(partenaireCourant)) {
            throw new TransfertHorsPartenaire();
        }

        // Seul un NON_CLOTURE peut être clôturé
        if (t.getStatut() != StatutTransfert.NON_CLOTURE) {
            throw new TransfertNonCloturable();
        }

        // Finalisation
        t.setReference(cmd.reference().trim());
        if (cmd.canal() != null && !cmd.canal().isBlank()) {
            t.setCanal(cmd.canal());
        }
        t.setStatut(StatutTransfert.EXECUTE);
        t.setCumulMois(t.getCumulMois() + t.getMontant());
        t.setReferenceVerification(genererReference());
        Transfert sauve = transferts.enregistrer(t);

        // Ajuste les compteurs du jour d'origine
        if (t.getAgentId() != null && t.getDateTransfert() != null) {
            compteurs.basculerNonClotureVersExecute(t.getAgentId(), t.getDateTransfert());
        }

        String montantFmt = String.format(java.util.Locale.FRENCH, "%,d", sauve.getMontant());
        return new VerificationResultat(true,
                "Transfert de " + montantFmt + " FCFA clôturé et exécuté (réf. " + sauve.getReference() + ").",
                0, 0, sauve.getMontant(), 0, 0, 0, sauve.getId());
    }

    private static String genererReference() {
        StringBuilder code = new StringBuilder("V");
        for (int i = 0; i < 6; i++) code.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        return code.toString();
    }
}