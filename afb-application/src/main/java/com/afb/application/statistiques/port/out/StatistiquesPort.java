package com.afb.domain.statistique.port.out;

import java.time.LocalDate;
import java.util.List;

/**
 * PORT DE SORTIE : agrégations statistiques dont le domaine a besoin.
 * Les lignes brutes ([statut,count] ; [année,mois,count] ; [id,nom,count])
 * sont renvoyées sous forme de tableaux d'objets, comme dans l'ancien backend.
 */
public interface StatistiquesPort {

    // Comptages
    long compterParPartenaire(Long partenaireId);
    long compterParAgentEntreDates(Long agentId, LocalDate debut, LocalDate fin);
    long compterParAgentEtStatut(Long agentId, String statut);

    // Répartition par statut : lignes [statut, count]
    List<Object[]> repartitionParStatutPartenaire(Long partenaireId);
    List<Object[]> repartitionParStatutAgent(Long agentId);
    List<Object[]> repartitionParStatutPlateforme();

    // Évolution mensuelle : lignes [année, mois, count]
    List<Object[]> parMoisPartenaire(Long partenaireId, LocalDate depuis);
    List<Object[]> parMoisAgent(Long agentId, LocalDate depuis);
    List<Object[]> parMoisPlateforme(LocalDate depuis);

    // Classements : lignes [id, nom, count]
    List<Object[]> topAgents(Long partenaireId, int limite);
    List<Object[]> topPartenaires(int limite);

    // Activité récente
    List<Object[]> activiteRecentePartenaire(Long partenaireId);
    List<Object[]> activiteRecenteAgent(Long agentId);
    List<Object[]> activiteRecentePlateforme();
}