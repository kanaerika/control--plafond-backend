package com.afb.application.transfert.usecase;

import com.afb.application.transfert.port.in.AnnulerRejeterUseCase;
import com.afb.application.transfert.port.in.TransfertResultat;
import com.afb.domain.agent.model.Agent;
import com.afb.domain.agent.port.out.AdminCourantPort;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
import com.afb.domain.transfert.exception.StatutTransfertInvalide;
import com.afb.domain.transfert.exception.TransfertHorsPartenaire;
import com.afb.domain.transfert.exception.TransfertIntrouvable;
import com.afb.domain.transfert.model.StatutTransfert;
import com.afb.domain.transfert.model.Transfert;
import com.afb.domain.transfert.model.ValidationMotif;
import com.afb.domain.transfert.port.out.CompteurJournalierPort;
import com.afb.domain.transfert.port.out.TransfertRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Annulation et rejet d'un transfert. Fidèle à l'ancien backend :
 *  - motif obligatoire (min. 10 caractères)
 *  - annuler : seul un EXECUTE peut être annulé → ANNULE, compteur annulés +1
 *  - rejeter : un EXECUTE ou un NON_CLOTURE → REJETE
 *      · si NON_CLOTURE : compteurs du jour d'origine (non clôturés -1, rejetés +1)
 *      · si EXECUTE : compteur rejetés +1 (aujourd'hui)
 *  - isolation par partenaire
 */
@Service
public class AnnulerRejeterService implements AnnulerRejeterUseCase {

    private final TransfertRepositoryPort transferts;
    private final CompteurJournalierPort compteurs;
    private final AgentRepositoryPort agents;
    private final AdminCourantPort adminCourant;

    public AnnulerRejeterService(TransfertRepositoryPort transferts,
                                 CompteurJournalierPort compteurs,
                                 AgentRepositoryPort agents,
                                 AdminCourantPort adminCourant) {
        this.transferts = transferts;
        this.compteurs = compteurs;
        this.agents = agents;
        this.adminCourant = adminCourant;
    }

    @Override
    @Transactional
    public TransfertResultat annuler(Long id, String motif) {
        ValidationMotif.valider(motif);
        Transfert t = chargerDansMonPartenaire(id);

        if (t.getStatut() != StatutTransfert.EXECUTE) {
            throw new StatutTransfertInvalide("Seul un transfert exécuté peut être annulé.");
        }
        t.setStatut(StatutTransfert.ANNULE);
        t.setMotif(motif.trim());
        Transfert sauve = transferts.enregistrer(t);

        if (t.getAgentId() != null) {
            compteurs.incrementerAnnules(t.getAgentId());
        }
        return versResultat(sauve);
    }

    @Override
    @Transactional
    public TransfertResultat rejeter(Long id, String motif) {
        ValidationMotif.valider(motif);
        Transfert t = chargerDansMonPartenaire(id);

        StatutTransfert avant = t.getStatut();
        if (avant != StatutTransfert.EXECUTE && avant != StatutTransfert.NON_CLOTURE) {
            throw new StatutTransfertInvalide(
                    "Seul un transfert exécuté ou non clôturé peut être rejeté.");
        }
        t.setStatut(StatutTransfert.REJETE);
        t.setMotif(motif.trim());
        Transfert sauve = transferts.enregistrer(t);

        if (avant == StatutTransfert.NON_CLOTURE) {
            if (t.getAgentId() != null && t.getDateTransfert() != null) {
                compteurs.basculerNonClotureVersRejete(t.getAgentId(), t.getDateTransfert());
            }
        } else {
            if (t.getAgentId() != null) {
                compteurs.incrementerRejetes(t.getAgentId());
            }
        }
        return versResultat(sauve);
    }

    // ------------------------------------------------------------------

    private Transfert chargerDansMonPartenaire(Long id) {
        Transfert t = transferts.trouverParId(id)
                .orElseThrow(() -> new TransfertIntrouvable(id));
        Long partenaireCourant = adminCourant.partenaireIdCourant();
        if (t.getPartenaireId() == null || !t.getPartenaireId().equals(partenaireCourant)) {
            throw new TransfertHorsPartenaire();
        }
        return t;
    }

    private TransfertResultat versResultat(Transfert t) {
        String agentNom = t.getAgentId() == null ? null
                : agents.trouverParId(t.getAgentId()).map(Agent::getNomComplet).orElse(null);
        return new TransfertResultat(
                t.getId(), t.getNomClient(), t.getDateNaissance(), t.getNaturePiece(),
                t.getNumeroPiece(), t.getMontant(), t.getPaysDestination(),
                t.getStatut() == null ? null : t.getStatut().getLibelle(),
                t.getReference(), t.getReferenceVerification(), t.getMotif(),
                t.getAgence(), t.getCanal(), t.getDateTransfert(), t.getCumulMois(),
                t.getPartenaireId(), t.getAgentId(), agentNom);
    }
}