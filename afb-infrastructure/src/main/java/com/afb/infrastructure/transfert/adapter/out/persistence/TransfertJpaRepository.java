package com.afb.infrastructure.transfert.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
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

    java.util.List<TransfertJpaEntity> findByAgentIdOrderByIdDesc(Long agentId);

    java.util.List<TransfertJpaEntity> findByPartenaireIdOrderByIdDesc(Long partenaireId);

    @Query("""
       SELECT t FROM TransfertJpaEntity t
       WHERE UPPER(t.nomClient) LIKE CONCAT('%', UPPER(:recherche), '%')
         AND t.partenaireId = :partenaireId
         AND t.id = (SELECT MAX(t2.id) FROM TransfertJpaEntity t2
                     WHERE UPPER(t2.nomClient) = UPPER(t.nomClient)
                       AND t2.partenaireId = :partenaireId)
       ORDER BY t.nomClient
       """)
    List<TransfertJpaEntity> rechercherClientsConnus(@Param("recherche") String recherche,
                                                    @Param("partenaireId") Long partenaireId);

    java.util.List<TransfertJpaEntity> findByPartenaireIdAndStatutOrderByIdDesc(
            Long partenaireId, com.afb.domain.transfert.model.StatutTransfert statut);

    java.util.List<TransfertJpaEntity> findByAgentIdAndDateTransfertOrderByIdDesc(
            Long agentId, LocalDate dateTransfert);
}