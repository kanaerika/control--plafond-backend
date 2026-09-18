package com.afb.domain.invitation.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Fabrique des jetons d'invitation et calcule leur empreinte.
 *
 * Le jeton part dans l'email ; seule l'empreinte est stockée. Les deux calculs
 * vivent ici pour qu'émission et vérification ne puissent pas diverger.
 */
public final class Jeton {

    private static final SecureRandom ALEA = new SecureRandom();
    private static final int OCTETS = 32; // 256 bits

    private Jeton() {
        // Classe utilitaire à méthodes statiques : elle ne doit pas être instanciée.
    }

    /** Jeton en clair, sûr pour une URL. */
    public static String generer() {
        byte[] brut = new byte[OCTETS];
        ALEA.nextBytes(brut);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(brut);
    }

    /** Empreinte SHA-256, seule valeur conservée en base. */
    public static String empreinte(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Jeton absent.");
        }
        try {
            byte[] h = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(h);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible sur cette JVM.", e);
        }
    }
}
