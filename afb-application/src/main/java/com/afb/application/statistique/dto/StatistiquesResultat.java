package com.afb.application.statistique.dto;

import java.util.List;

/** Objets de résultat des tableaux de bord statistiques. */
public class StatistiquesResultat {

    public record Kpi(String libelle, long valeur, String variante) {}

    public record PointTemporel(String periode, long valeur) {}

    public record Part(String libelle, long valeur, String couleur) {}

    public record Classement(Long id, String nom, long total) {}

    public record TransfertResume(
            Long id, String nomClient, long montant,
            String statut, String date, String agentNom) {}

    public record Reponse(
            String portee,
            List<Kpi> kpis,
            List<Part> repartitionOperations,
            List<PointTemporel> evolution,
            List<Classement> classement,
            List<TransfertResume> activiteRecente) {}
}