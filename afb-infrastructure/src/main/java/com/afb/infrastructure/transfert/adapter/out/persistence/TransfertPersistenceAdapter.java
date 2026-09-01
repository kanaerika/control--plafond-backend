package com.afb.infrastructure.transfert.adapter.out.persistence;

import com.afb.domain.transfert.model.Transfert;
import com.afb.domain.transfert.port.out.TransfertRepositoryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class TransfertPersistenceAdapter implements TransfertRepositoryPort {

    private final TransfertJpaRepository jpa;

    public TransfertPersistenceAdapter(TransfertJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public long cumulMensuel(String nomClient, String numeroPiece, String dateNaissance,
                             LocalDate debut, LocalDate fin) {
        return jpa.cumulMensuel(nomClient, numeroPiece, dateNaissance, debut, fin);
    }

    @Override
    public Transfert enregistrer(Transfert t) {
        TransfertJpaEntity e = t.getId() != null
                ? jpa.findById(t.getId()).orElseGet(TransfertJpaEntity::new)
                : new TransfertJpaEntity();
        appliquer(t, e);
        return versDomaine(jpa.save(e));
    }

    @Override
    public Transfert dernierTransfertDuClient(String nomClient) {
        return jpa.findFirstByNomClientIgnoreCaseOrderByIdDesc(nomClient)
                .map(TransfertPersistenceAdapter::versDomaine).orElse(null);
    }

    private static Transfert versDomaine(TransfertJpaEntity e) {
        Transfert t = new Transfert();
        t.setId(e.getId());
        t.setNomClient(e.getNomClient());
        t.setDateNaissance(e.getDateNaissance());
        t.setNaturePiece(e.getNaturePiece());
        t.setNumeroPiece(e.getNumeroPiece());
        t.setMontant(e.getMontant());
        t.setPaysDestination(e.getPaysDestination());
        t.setStatut(e.getStatut());
        t.setReference(e.getReference());
        t.setReferenceVerification(e.getReferenceVerification());
        t.setMotif(e.getMotif());
        t.setAgence(e.getAgence());
        t.setCanal(e.getCanal());
        t.setDateTransfert(e.getDateTransfert());
        t.setCumulMois(e.getCumulMois());
        t.setPartenaireId(e.getPartenaireId());
        t.setAgentId(e.getAgentId());
        return t;
    }

    private static void appliquer(Transfert t, TransfertJpaEntity e) {
        e.setNomClient(t.getNomClient());
        e.setDateNaissance(t.getDateNaissance());
        e.setNaturePiece(t.getNaturePiece());
        e.setNumeroPiece(t.getNumeroPiece());
        e.setMontant(t.getMontant());
        e.setPaysDestination(t.getPaysDestination());
        e.setStatut(t.getStatut());
        e.setReference(t.getReference());
        e.setReferenceVerification(t.getReferenceVerification());
        e.setMotif(t.getMotif());
        e.setAgence(t.getAgence());
        e.setCanal(t.getCanal());
        e.setDateTransfert(t.getDateTransfert());
        e.setCumulMois(t.getCumulMois());
        e.setPartenaireId(t.getPartenaireId());
        e.setAgentId(t.getAgentId());
    }
}