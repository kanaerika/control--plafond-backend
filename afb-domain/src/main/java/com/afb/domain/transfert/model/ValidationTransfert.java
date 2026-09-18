package com.afb.domain.transfert.model;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Règles de saisie d'un transfert (PUR), communes à la vérification, à
 * l'exécution directe et au contrôle de plafond d'un client.
 *
 * Avant ces règles, un montant négatif était déclaré « valide » et enregistré,
 * un nom de client vide passait, et une date de naissance absente faisait
 * échouer la requête sur une erreur technique au lieu d'un message lisible.
 */
public final class ValidationTransfert {

    /** Format de l'interface et de la majorité des données : « 20-05-2007 ». */
    private static final DateTimeFormatter FORMAT_CANONIQUE =
            DateTimeFormatter.ofPattern("dd-MM-uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final Pattern JOUR_MOIS_ANNEE = Pattern.compile("(\\d{2})[-/.](\\d{2})[-/.](\\d{4})");
    private static final Pattern ANNEE_MOIS_JOUR = Pattern.compile("(\\d{4})-(\\d{2})-(\\d{2})");

    private ValidationTransfert() {
        // Classe utilitaire à méthodes statiques : elle ne doit pas être instanciée.
    }

    public static void valider(String nomClient, long montant, String paysDestination) {
        if (nomClient == null || nomClient.isBlank()) {
            throw new IllegalArgumentException("Le nom du client est obligatoire.");
        }
        if (montant <= 0) {
            throw new IllegalArgumentException("Le montant doit être supérieur à zéro.");
        }
        if (paysDestination == null || paysDestination.isBlank()) {
            throw new IllegalArgumentException("Le pays de destination est obligatoire.");
        }
    }

    /**
     * Valide la date de naissance et la ramène au format unique « JJ-MM-AAAA ».
     *
     * Le cumul mensuel retrouve un client en comparant sa date de naissance
     * comme du texte : « 01-01-1990 » et « 01/01/1990 » y désignaient deux
     * clients différents, et un même client pouvait dépasser son plafond en
     * variant simplement le séparateur. Toute date passe donc par ici avant
     * d'être comparée ou enregistrée.
     */
    public static String normaliserDateNaissance(String saisie) {
        if (saisie == null || saisie.isBlank()) {
            throw new IllegalArgumentException("La date de naissance du client est obligatoire.");
        }
        String s = saisie.trim();
        LocalDate date;
        try {
            Matcher jma = JOUR_MOIS_ANNEE.matcher(s);
            Matcher amj = ANNEE_MOIS_JOUR.matcher(s);
            if (jma.matches()) {
                date = LocalDate.of(Integer.parseInt(jma.group(3)),
                        Integer.parseInt(jma.group(2)), Integer.parseInt(jma.group(1)));
            } else if (amj.matches()) {
                date = LocalDate.of(Integer.parseInt(amj.group(1)),
                        Integer.parseInt(amj.group(2)), Integer.parseInt(amj.group(3)));
            } else {
                throw new IllegalArgumentException(
                        "Date de naissance invalide : format attendu JJ-MM-AAAA.");
            }
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Date de naissance invalide : « " + s + " » n'existe pas.");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La date de naissance ne peut pas être dans le futur.");
        }
        return date.format(FORMAT_CANONIQUE);
    }
}
