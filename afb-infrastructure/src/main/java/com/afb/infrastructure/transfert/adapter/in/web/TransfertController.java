package com.afb.infrastructure.transfert.adapter.in.web;

import com.afb.application.transfert.port.in.AnnulerRejeterUseCase;
import com.afb.application.transfert.port.in.CloturerCommande;
import com.afb.application.transfert.port.in.CloturerTransfertUseCase;
import com.afb.application.transfert.port.in.ExecuterTransfertUseCase;
import com.afb.application.transfert.port.in.ExecutionCommande;
import com.afb.application.transfert.port.in.TransfertResultat;
import com.afb.application.transfert.port.in.VerificationCommande;
import com.afb.application.transfert.port.in.VerificationResultat;
import com.afb.application.transfert.port.in.VerifierTransfertUseCase;
import com.afb.domain.agent.model.Agent;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Écriture : vérification, exécution, clôture, annulation et rejet.
 *
 * La consultation vit dans {@link ConsultationTransfertController}. Réunies,
 * les deux moitiés demandaient neuf dépendances au même constructeur (Sonar
 * S107) ; séparées, chaque contrôleur ne dépend que de ce qu'il utilise.
 */
@RestController
@RequestMapping("/api/v1/transferts")
@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
public class TransfertController {

    private final VerifierTransfertUseCase verifier;
    private final ExecuterTransfertUseCase executerTransfert;
    private final CloturerTransfertUseCase cloturerTransfert;
    private final AnnulerRejeterUseCase annulerRejeter;
    private final AgentRepositoryPort agents;

    public TransfertController(VerifierTransfertUseCase verifier,
                               ExecuterTransfertUseCase executerTransfert,
                               CloturerTransfertUseCase cloturerTransfert,
                               AnnulerRejeterUseCase annulerRejeter,
                               AgentRepositoryPort agents) {
        this.verifier = verifier;
        this.executerTransfert = executerTransfert;
        this.cloturerTransfert = cloturerTransfert;
        this.annulerRejeter = annulerRejeter;
        this.agents = agents;
    }

    /**
     * Agent connecté résolu depuis le claim « email » du JWT — jamais depuis un
     * identifiant fourni par le client. C'est ce qui alimente agentId /
     * partenaireId sur les transferts créés (sans quoi la colonne partenaire_id
     * part à NULL et l'insert échoue).
     */
    private Agent agentCourant(Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            email = jwt.getClaimAsString("preferred_username");
        }
        final String recherche = email;
        return agents.trouverParEmail(recherche)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun agent local associé à « " + recherche + " »."));
    }

    @PostMapping("/verification")
    public VerificationResultat verifier(@AuthenticationPrincipal Jwt jwt,
                                         @RequestBody VerificationRequete r) {
        Agent agent = agentCourant(jwt);
        return verifier.verifier(new VerificationCommande(
                r.nomClient(), r.dateNaissance(), r.naturePiece(), r.numeroPiece(),
                r.montant(), r.paysDestination(),
                agent.getId(), agent.getPartenaireId(), agent.getAgence()));
    }

    @PostMapping("/execution")
    public VerificationResultat executer(@AuthenticationPrincipal Jwt jwt,
                                         @RequestBody ExecutionRequete r) {
        // Si un transfertId est fourni, c'est une clôture (gérée ailleurs) ; ici, exécution directe.
        Agent agent = agentCourant(jwt);
        return executerTransfert.executer(new ExecutionCommande(
                r.nomClient(), r.dateNaissance(), r.naturePiece(), r.numeroPiece(),
                r.montant(), r.paysDestination(), r.reference(), r.canal(),
                agent.getId(), agent.getPartenaireId(), agent.getAgence()));
    }

    @PostMapping("/cloture")
    public VerificationResultat cloturer(@RequestBody ClotureRequete r) {
        return cloturerTransfert.cloturer(new CloturerCommande(
                r.transfertId(), r.reference(), r.canal()));
    }

    @PostMapping("/{id}/annulation")
    public TransfertResultat annuler(@PathVariable Long id, @RequestBody MotifRequete r) {
        return annulerRejeter.annuler(id, r.motif());
    }

    @PostMapping("/{id}/rejet")
    public TransfertResultat rejeter(@PathVariable Long id, @RequestBody MotifRequete r) {
        return annulerRejeter.rejeter(id, r.motif());
    }

    public record VerificationRequete(
            String nomClient, String dateNaissance, String naturePiece, String numeroPiece,
            long montant, String paysDestination,
            Long agentId, Long partenaireId, String agenceAgent) {}

    public record ExecutionRequete(
            String nomClient, String dateNaissance, String naturePiece, String numeroPiece,
            long montant, String paysDestination, String reference, String canal,
            Long agentId, Long partenaireId, String agenceAgent) {}

    public record ClotureRequete(Long transfertId, String reference, String canal) {}

    public record MotifRequete(String motif) {}
}
