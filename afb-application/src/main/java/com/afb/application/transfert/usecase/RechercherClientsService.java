package com.afb.application.transfert.usecase;

import com.afb.application.transfert.port.in.ClientConnuResultat;
import com.afb.application.transfert.port.in.RechercherClientsUseCase;
import com.afb.domain.agent.port.out.AdminCourantPort;
import com.afb.domain.transfert.port.out.TransfertRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Auto-complétion des clients connus.
 *
 * Fidèle à l'ancien backend : min. 2 caractères, max 6 résultats, recherche par
 * nom. Ajout : isolation par partenaire (l'agent ne voit que les clients de son
 * partenaire), conformément à la règle d'isolation du projet.
 */
@Service
public class RechercherClientsService implements RechercherClientsUseCase {

    private final TransfertRepositoryPort transferts;
    private final AdminCourantPort adminCourant;

    public RechercherClientsService(TransfertRepositoryPort transferts,
                                    AdminCourantPort adminCourant) {
        this.transferts = transferts;
        this.adminCourant = adminCourant;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientConnuResultat> clientsConnus(String prefixe) {
        if (prefixe == null || prefixe.trim().length() < 2) {
            return List.of();
        }
        String recherche = prefixe.trim().replaceAll("\\s{2,}", " ");
        Long partenaireId = adminCourant.partenaireIdCourant();

        return transferts.rechercherClientsConnus(recherche, partenaireId).stream()
                .limit(6)
                .map(t -> new ClientConnuResultat(
                        t.getNomClient(),
                        t.getDateNaissance(),
                        t.getNaturePiece(),
                        t.getNumeroPiece()))
                .toList();
    }
}