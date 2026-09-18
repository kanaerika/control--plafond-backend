package com.afb.commons;

/**
 * Petits utilitaires d'URL partagés par les adaptateurs.
 */
public final class Urls {

    private Urls() {
        // Classe utilitaire : aucune instance.
    }

    /**
     * Retire les « / » finaux d'une URL de base, pour pouvoir y concaténer un
     * chemin sans produire de double séparateur.
     *
     * Écrit sans expression régulière à dessein (Sonar S5852) : {@code "/+$"}
     * n'est pas ancré à gauche, le moteur relance donc la recherche à chaque
     * position et rebrousse chemin à chaque fois — coût quadratique sur une
     * chaîne faite de barres obliques. Le parcours ci-dessous est linéaire.
     */
    public static String sansSlashFinal(String url) {
        if (url == null) {
            return null;
        }
        int fin = url.length();
        while (fin > 0 && url.charAt(fin - 1) == '/') {
            fin--;
        }
        return url.substring(0, fin);
    }
}
