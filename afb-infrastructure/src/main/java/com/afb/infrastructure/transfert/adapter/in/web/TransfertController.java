package com.afb.infrastructure.transfert.adapter.in.web;

import com.afb.application.transfert.port.in.*;
import com.afb.domain.agent.model.Agent;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transferts")
@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
public class TransfertController {
    private final ConsulterBilanUseCase consulterBilan;
    private final ExecuterTransfertUseCase executerTransfert;
    private final VerifierTransfertUseCase verifier;
    private final RechercherClientsUseCase rechercherClients;
    private final CloturerTransfertUseCase cloturerTransfert;
    private final ConsulterTransfertsUseCase consulterTransferts;
    private final AnnulerRejeterUseCase annulerRejeter;
    private final ConsulterPlafondClientUseCase consulterPlafondClient;
    private final AgentRepositoryPort agents;
    public TransfertController(VerifierTransfertUseCase verifier,
                              RechercherClientsUseCase rechercherClients,
                              CloturerTransfertUseCase cloturerTransfert,
                              ConsulterTransfertsUseCase consulterTransferts,
                              AnnulerRejeterUseCase annulerRejeter,
                              ConsulterBilanUseCase consulterBilan,
                              ExecuterTransfertUseCase executerTransfert,
                              ConsulterPlafondClientUseCase consulterPlafondClient,
                              AgentRepositoryPort agents) {
        this.verifier = verifier;
        this.rechercherClients = rechercherClients;
        this.cloturerTransfert = cloturerTransfert;
        this.consulterTransferts = consulterTransferts;
        this.annulerRejeter = annulerRejeter;
        this.consulterBilan = consulterBilan;
        this.executerTransfert = executerTransfert;
        this.consulterPlafondClient = consulterPlafondClient;
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
    @GetMapping("/clients-connus")
    public List<ClientConnuResultat> clientsConnus(@RequestParam(defaultValue = "") String q) {
        return rechercherClients.clientsConnus(q);
    }

    @GetMapping("/plafond-client")
    public PlafondClientResultat plafondClient(@RequestParam String nomClient,
                                               @RequestParam String numeroPiece,
                                               @RequestParam String dateNaissance) {
        return consulterPlafondClient.pour(nomClient, numeroPiece, dateNaissance);
    }
@PostMapping("/execution")
public VerificationResultat executer(@AuthenticationPrincipal Jwt jwt, @RequestBody ExecutionRequete r) {
    // Si un transfertId est fourni, c'est une clôture (gérée ailleurs) ; ici, exécution directe.
    Agent agent = agentCourant(jwt);
    return executerTransfert.executer(new com.afb.application.transfert.port.in.ExecutionCommande(
            r.nomClient(), r.dateNaissance(), r.naturePiece(), r.numeroPiece(),
            r.montant(), r.paysDestination(), r.reference(), r.canal(),
            agent.getId(), agent.getPartenaireId(), agent.getAgence()));
}


    @GetMapping("/historique")
public List<TransfertResultat> historique(@RequestParam(defaultValue = "") String q) {
    return consulterTransferts.historique(q);
}

@GetMapping("/non-clotures")
public List<TransfertResultat> nonClotures(@RequestParam(defaultValue = "") String q) {
    return consulterTransferts.nonClotures(q);
}

@GetMapping("/annulables")
public List<TransfertResultat> annulables(@RequestParam(defaultValue = "") String q) {
    return consulterTransferts.annulables(q);
}
@GetMapping("/{id}")
public TransfertResultat detail(@PathVariable Long id) {
    return consulterTransferts.detail(id);
}
@GetMapping("/bilan")
public BilanResultat bilan() {
    return consulterBilan.bilanDuJour();
}
public record VerificationRequete(
            String nomClient, String dateNaissance, String naturePiece, String numeroPiece,
            long montant, String paysDestination,
            Long agentId, Long partenaireId, String agenceAgent) {}
    public record ClotureRequete(Long transfertId, String reference, String canal) {}
    public record MotifRequete(String motif) {}

    public record ExecutionRequete(
            String nomClient, String dateNaissance, String naturePiece, String numeroPiece,
            long montant, String paysDestination, String reference, String canal,
            Long agentId, Long partenaireId, String agenceAgent) {}
}