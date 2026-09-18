package com.afb.infrastructure.transfert.adapter.in.web;

import com.afb.application.transfert.port.in.BilanResultat;
import com.afb.application.transfert.port.in.ClientConnuResultat;
import com.afb.application.transfert.port.in.ConsulterBilanUseCase;
import com.afb.application.transfert.port.in.ConsulterPlafondClientUseCase;
import com.afb.application.transfert.port.in.ConsulterTransfertsUseCase;
import com.afb.application.transfert.port.in.PlafondClientResultat;
import com.afb.application.transfert.port.in.RechercherClientsUseCase;
import com.afb.application.transfert.port.in.TransfertResultat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Consultation : historique, dossiers en attente, bilan du jour, clients connus
 * et plafond d'un client. Pendant en lecture de {@link TransfertController}.
 */
@RestController
@RequestMapping("/api/v1/transferts")
@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
public class ConsultationTransfertController {

    private final ConsulterTransfertsUseCase consulterTransferts;
    private final ConsulterBilanUseCase consulterBilan;
    private final RechercherClientsUseCase rechercherClients;
    private final ConsulterPlafondClientUseCase consulterPlafondClient;

    public ConsultationTransfertController(ConsulterTransfertsUseCase consulterTransferts,
                                           ConsulterBilanUseCase consulterBilan,
                                           RechercherClientsUseCase rechercherClients,
                                           ConsulterPlafondClientUseCase consulterPlafondClient) {
        this.consulterTransferts = consulterTransferts;
        this.consulterBilan = consulterBilan;
        this.rechercherClients = rechercherClients;
        this.consulterPlafondClient = consulterPlafondClient;
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

    @GetMapping("/bilan")
    public BilanResultat bilan() {
        return consulterBilan.bilanDuJour();
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

    /**
     * Placé après les chemins littéraux ci-dessus ; Spring fait de toute façon
     * primer « /bilan » ou « /annulables » sur ce gabarit.
     */
    @GetMapping("/{id}")
    public TransfertResultat detail(@PathVariable Long id) {
        return consulterTransferts.detail(id);
    }
}
