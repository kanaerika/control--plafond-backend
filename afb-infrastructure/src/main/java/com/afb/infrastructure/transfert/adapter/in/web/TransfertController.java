package com.afb.infrastructure.transfert.adapter.in.web;

import com.afb.application.transfert.port.in.VerificationCommande;
import com.afb.application.transfert.port.in.VerificationResultat;
import com.afb.application.transfert.port.in.VerifierTransfertUseCase;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transferts")
public class TransfertController {

    private final VerifierTransfertUseCase verifier;

    public TransfertController(VerifierTransfertUseCase verifier) {
        this.verifier = verifier;
    }

    @PostMapping("/verification")
    public VerificationResultat verifier(@RequestBody VerificationRequete r) {
        return verifier.verifier(new VerificationCommande(
                r.nomClient(), r.dateNaissance(), r.naturePiece(), r.numeroPiece(),
                r.montant(), r.paysDestination(),
                r.agentId(), r.partenaireId(), r.agenceAgent()));
    }

    public record VerificationRequete(
            String nomClient, String dateNaissance, String naturePiece, String numeroPiece,
            long montant, String paysDestination,
            Long agentId, Long partenaireId, String agenceAgent) {}
}