package com.afb.application.statistique.usecase;

import com.afb.application.statistique.dto.StatistiquesResultat.*;
import com.afb.application.statistique.port.in.ConsulterStatistiquesUseCase;
import com.afb.domain.statistique.port.out.StatistiquesPort;
import com.afb.domain.statistique.port.out.UtilisateurCourantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Statistiques adaptées au rôle (fidèle à l'ancien StatistiquesService) :
 *  - SUPER_ADMIN → plateforme entière
 *  - ADMIN_PARTENAIRE → son organisation
 *  - AGENT → ses propres transferts
 */
@Service
public class ConsulterStatistiquesService implements ConsulterStatistiquesUseCase {

    private static final String C_VERT = "#128C4A";
    private static final String C_ROUGE = "#C8102E";
    private static final String C_ORANGE = "#E08A00";
    private static final String C_BLEU = "#1F6FEB";
    private static final String C_VIOLET = "#7C4DBC";
    private static final String C_GRIS = "#6B7280";

    private final StatistiquesPort stats;
    private final UtilisateurCourantPort utilisateur;

    public ConsulterStatistiquesService(StatistiquesPort stats,
                                        UtilisateurCourantPort utilisateur) {
        this.stats = stats;
        this.utilisateur = utilisateur;
    }

    @Override
    @Transactional(readOnly = true)
    public Reponse pourUtilisateurCourant() {
        return switch (utilisateur.roleCourant()) {
            case "SUPER_ADMIN" -> plateforme();
            case "ADMIN_PARTENAIRE" -> partenaire();
            case "AGENT" -> agent();
            default -> throw new IllegalStateException("Rôle inconnu pour les statistiques.");
        };
    }

    private Reponse partenaire() {
        Long pid = utilisateur.partenaireIdCourant();
        List<Object[]> parStatut = stats.repartitionParStatutPartenaire(pid);

        // Libellés lus tels quels par le tableau de bord : ne pas les renommer
        // sans modifier dashboard.component.ts.
        List<Kpi> kpis = List.of(
                new Kpi("Agents", stats.compterAgentsPartenaire(pid), "info"),
                new Kpi("Transferts", total(parStatut), "principal"),
                new Kpi("Bloqués", compte(parStatut, "REFUSE_PLAFOND"), "attention")
        );
        return new Reponse("PARTENAIRE", kpis,
                repartition(parStatut),
                evolution(stats.parMoisPartenaire(pid, douzeMoisAvant())),
                classement(stats.topAgents(pid, 5)),
                activite(stats.activiteRecentePartenaire(pid)));
    }

    private Reponse agent() {
        Long aid = utilisateur.agentIdCourant();
        LocalDate auj = LocalDate.now();

        long jour = stats.compterParAgentEntreDates(aid, auj, auj);
        long semaine = stats.compterParAgentEntreDates(aid, auj.minusDays(6), auj);
        long mois = stats.compterParAgentEntreDates(aid, auj.withDayOfMonth(1), auj);
        long executes = stats.compterParAgentEtStatut(aid, "EXECUTE");

        List<Kpi> kpis = List.of(
                new Kpi("Aujourd'hui", jour, "principal"),
                new Kpi("Cette semaine", semaine, "info"),
                new Kpi("Ce mois", mois, "succes"),
                new Kpi("Total exécutés", executes, "principal")
        );
        return new Reponse("AGENT", kpis,
                repartition(stats.repartitionParStatutAgent(aid)),
                evolution(stats.parMoisAgent(aid, douzeMoisAvant())),
                List.of(),
                activite(stats.activiteRecenteAgent(aid)));
    }

    private Reponse plateforme() {
        List<Object[]> parStatut = stats.repartitionParStatutPlateforme();
        // Laissée vide, cette liste affichait trois cartes sans chiffre sur le
        // tableau de bord d'Afriland.
        List<Kpi> kpis = List.of(
                new Kpi("Agents", stats.compterAgentsPlateforme(), "info"),
                new Kpi("Transferts", total(parStatut), "principal"),
                new Kpi("Bloqués", compte(parStatut, "REFUSE_PLAFOND"), "attention")
        );
        return new Reponse("PLATEFORME", kpis,
                repartition(parStatut),
                evolution(stats.parMoisPlateforme(douzeMoisAvant())),
                classement(stats.topPartenaires(5)),
                activite(stats.activiteRecentePlateforme()));
    }

    // ---------- Helpers (identiques à l'ancien) ----------

    private LocalDate douzeMoisAvant() {
        return LocalDate.now().minusMonths(11).withDayOfMonth(1);
    }

    private List<Part> repartition(List<Object[]> lignes) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] l : lignes) {
            map.put(String.valueOf(l[0]), ((Number) l[1]).longValue());
        }
        List<Part> parts = new ArrayList<>();
        // Tous les statuts : il manquait « Non clôturé » et « Refusé (plafond) »,
        // si bien que la répartition ne totalisait pas le nombre de transferts.
        parts.add(new Part("Exécutés", map.getOrDefault("EXECUTE", 0L), C_VERT));
        parts.add(new Part("Non clôturés", map.getOrDefault("NON_CLOTURE", 0L), C_VIOLET));
        parts.add(new Part("En cours", map.getOrDefault("EN_COURS", 0L), C_BLEU));
        parts.add(new Part("Annulés", map.getOrDefault("ANNULE", 0L), C_ORANGE));
        parts.add(new Part("Rejetés", map.getOrDefault("REJETE", 0L), C_ROUGE));
        parts.add(new Part("Refusés (plafond)", map.getOrDefault("REFUSE_PLAFOND", 0L), C_GRIS));
        return parts;
    }

    private static long total(List<Object[]> lignesParStatut) {
        return lignesParStatut.stream().mapToLong(l -> ((Number) l[1]).longValue()).sum();
    }

    private static long compte(List<Object[]> lignesParStatut, String statut) {
        return lignesParStatut.stream()
                .filter(l -> statut.equals(String.valueOf(l[0])))
                .mapToLong(l -> ((Number) l[1]).longValue())
                .sum();
    }

    private List<PointTemporel> evolution(List<Object[]> lignes) {
        Map<String, Long> valeurs = new HashMap<>();
        for (Object[] l : lignes) {
            int annee = ((Number) l[0]).intValue();
            int mois = ((Number) l[1]).intValue();
            valeurs.put(annee + "-" + mois, ((Number) l[2]).longValue());
        }
        List<PointTemporel> serie = new ArrayList<>();
        LocalDate curseur = douzeMoisAvant();
        for (int i = 0; i < 12; i++) {
            String cle = curseur.getYear() + "-" + curseur.getMonthValue();
            String libelle = curseur.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            serie.add(new PointTemporel(libelle, valeurs.getOrDefault(cle, 0L)));
            curseur = curseur.plusMonths(1);
        }
        return serie;
    }

    private List<Classement> classement(List<Object[]> lignes) {
        List<Classement> res = new ArrayList<>();
        for (Object[] l : lignes) {
            res.add(new Classement(((Number) l[0]).longValue(),
                    (String) l[1], ((Number) l[2]).longValue()));
        }
        return res;
    }

    private List<TransfertResume> activite(List<Object[]> lignes) {
        List<TransfertResume> res = new ArrayList<>();
        for (Object[] l : lignes) {
            res.add(new TransfertResume(
                    ((Number) l[0]).longValue(),
                    (String) l[1],
                    ((Number) l[2]).longValue(),
                    String.valueOf(l[3]),
                    String.valueOf(l[4]),
                    l[5] != null ? (String) l[5] : "—"));
        }
        return res;
    }
}