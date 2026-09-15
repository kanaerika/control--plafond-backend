package com.afb.infrastructure.statistique.adapter.out.persistence;

import com.afb.infrastructure.transfert.adapter.out.persistence.TransfertJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Requêtes d'agrégation pour les statistiques. Fidèles à l'ancien backend,
 * adaptées aux noms de champs de TransfertJpaEntity (partenaireId, agentId scalaires).
 */
public interface StatistiquesJpaRepository extends JpaRepository<TransfertJpaEntity, Long> {

    // ---- Comptages ----
    long countByPartenaireId(Long partenaireId);

    long countByAgentIdAndDateTransfertBetween(Long agentId, LocalDate debut, LocalDate fin);

    long countByAgentIdAndStatut(Long agentId,
            com.afb.domain.transfert.model.StatutTransfert statut);

    // ---- Répartition par statut : [statut, count] ----
    @Query("SELECT t.statut, COUNT(t) FROM TransfertJpaEntity t WHERE t.partenaireId = :pid GROUP BY t.statut")
    List<Object[]> repartitionParStatutPartenaire(@Param("pid") Long partenaireId);

    @Query("SELECT t.statut, COUNT(t) FROM TransfertJpaEntity t WHERE t.agentId = :aid GROUP BY t.statut")
    List<Object[]> repartitionParStatutAgent(@Param("aid") Long agentId);

    @Query("SELECT t.statut, COUNT(t) FROM TransfertJpaEntity t GROUP BY t.statut")
    List<Object[]> repartitionParStatutPlateforme();

    // ---- Évolution mensuelle : [année, mois, count] ----
    @Query("""
           SELECT EXTRACT(YEAR FROM t.dateTransfert), EXTRACT(MONTH FROM t.dateTransfert), COUNT(t)
           FROM TransfertJpaEntity t
           WHERE t.partenaireId = :pid AND t.dateTransfert >= :depuis
           GROUP BY EXTRACT(YEAR FROM t.dateTransfert), EXTRACT(MONTH FROM t.dateTransfert)
           ORDER BY EXTRACT(YEAR FROM t.dateTransfert), EXTRACT(MONTH FROM t.dateTransfert)
           """)
    List<Object[]> parMoisPartenaire(@Param("pid") Long partenaireId, @Param("depuis") LocalDate depuis);

    @Query("""
           SELECT EXTRACT(YEAR FROM t.dateTransfert), EXTRACT(MONTH FROM t.dateTransfert), COUNT(t)
           FROM TransfertJpaEntity t
           WHERE t.agentId = :aid AND t.dateTransfert >= :depuis
           GROUP BY EXTRACT(YEAR FROM t.dateTransfert), EXTRACT(MONTH FROM t.dateTransfert)
           ORDER BY EXTRACT(YEAR FROM t.dateTransfert), EXTRACT(MONTH FROM t.dateTransfert)
           """)
    List<Object[]> parMoisAgent(@Param("aid") Long agentId, @Param("depuis") LocalDate depuis);

    @Query("""
           SELECT EXTRACT(YEAR FROM t.dateTransfert), EXTRACT(MONTH FROM t.dateTransfert), COUNT(t)
           FROM TransfertJpaEntity t
           WHERE t.dateTransfert >= :depuis
           GROUP BY EXTRACT(YEAR FROM t.dateTransfert), EXTRACT(MONTH FROM t.dateTransfert)
           ORDER BY EXTRACT(YEAR FROM t.dateTransfert), EXTRACT(MONTH FROM t.dateTransfert)
           """)
    List<Object[]> parMoisPlateforme(@Param("depuis") LocalDate depuis);

    // ---- Classements : [id, nom, count] ----
    // NB : l'entité ne porte que agentId (pas le nom). Le nom sera résolu côté adaptateur
    // via une jointure applicative ; ici on renvoie [agentId, count] puis on complète.
    @Query("""
           SELECT t.agentId, COUNT(t)
           FROM TransfertJpaEntity t
           WHERE t.partenaireId = :pid AND t.agentId IS NOT NULL
           GROUP BY t.agentId
           ORDER BY COUNT(t) DESC
           """)
    List<Object[]> topAgents(@Param("pid") Long partenaireId, Pageable pageable);

    @Query("""
           SELECT t.partenaireId, COUNT(t)
           FROM TransfertJpaEntity t
           WHERE t.partenaireId IS NOT NULL
           GROUP BY t.partenaireId
           ORDER BY COUNT(t) DESC
           """)
    List<Object[]> topPartenaires(Pageable pageable);

    // ---- Activité récente ----
    List<TransfertJpaEntity> findTop8ByPartenaireIdOrderByIdDesc(Long partenaireId);
    List<TransfertJpaEntity> findTop8ByAgentIdOrderByIdDesc(Long agentId);
    List<TransfertJpaEntity> findTop8ByOrderByIdDesc();
}