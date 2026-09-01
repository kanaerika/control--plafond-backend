package com.afb.domain.transfert.model;

/** Règles de validation d'une pièce d'identité (PUR). CNI ancienne=18 chiffres, nouvelle=2 lettres+8 chiffres. */
public final class ValidationPiece {

    private ValidationPiece() {}

    public static void valider(String nature, String numero) {
        String n = numero == null ? "" : numero.trim();
        if (!n.matches("[A-Za-z0-9]+")) {
            throw new IllegalArgumentException(
                    "Le n° de pièce ne doit contenir que des lettres et des chiffres (sans espaces).");
        }
        if ("Carte Nationale d'Identité".equals(nature)) {
            boolean nouvelleCarte = n.matches("[A-Za-z]{2}\\d{8}");
            boolean ancienneCarte = n.matches("\\d{18}");
            if (!nouvelleCarte && !ancienneCarte) {
                throw new IllegalArgumentException(
                        "N° de CNI invalide : attendu 18 chiffres (ancienne carte) "
                        + "ou 2 lettres suivies de 8 chiffres (nouvelle carte).");
            }
            return;
        }
        int min = switch (nature == null ? "" : nature) {
            case "Passeport" -> 7;
            default -> 6;
        };
        if (n.length() < min) {
            throw new IllegalArgumentException(
                    "N° de pièce trop court pour une pièce de type « " + nature
                    + " » (minimum " + min + " caractères).");
        }
    }
}