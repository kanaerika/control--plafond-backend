package com.afb.infrastructure.transfert.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface TransfertJpaRepository extends JpaRepository<TransfertJpaEntity, Long> {

    @Query("""
           SELECT COALESCE(SUM(t.montant), 0)
           FROM TransfertJpaEntity t
           WHERE UPPER(t.nomClient) = UPPER(:nomClient)
             AND UPPER(t.numeroPiece) = UPPER(:numeroPiece)
             AND t.dateNaissance = :dateNaissance
             AND t.statut = com.afb.domain.transfert.model.StatutTransfert.EXECUTE
             AND t.dateTransfert BETWEEN :debut AND :fin
           """)
    long cumulMensuel(@Param("nomClient") String nomClient,
                      @Param("numeroPiece") String numeroPiece,
                      @Param("dateNaissance") String dateNaissance,
                      @Param("debut") LocalDate debut,
                      @Param("fin") LocalDate fin);

    Optional<TransfertJpaEntity> findFirstByNomClientIgnoreCaseOrderByIdDesc(String nomClient);
}