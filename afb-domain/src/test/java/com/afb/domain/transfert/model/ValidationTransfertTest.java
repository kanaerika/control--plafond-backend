package com.afb.domain.transfert.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Clock;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationTransfertTest {

    private static final String CLIENT = "CLIENT";
    private static final String PAYS = "France";

    /**
     * Le cumul mensuel compare la date comme du texte : si ces écritures ne
     * donnaient pas la même valeur, un client pourrait dépasser son plafond en
     * changeant simplement de séparateur.
     */
    @ParameterizedTest
    @ValueSource(strings = {"15-01-1990", "15/01/1990", "15.01.1990", "1990-01-15", "  15-01-1990 "})
    void toutesLesEcrituresDUneMemeDateDonnentLaMemeValeur(String saisie) {
        assertEquals("15-01-1990", ValidationTransfert.normaliserDateNaissance(saisie));
    }

    @ParameterizedTest
    @ValueSource(strings = {"31-02-1990", "00-01-1990", "15-13-1990", "hier", "1990", "15011990"})
    void dateInvalideRefusee(String saisie) {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationTransfert.normaliserDateNaissance(saisie));
    }

    @Test
    void dateAbsenteOuFutureRefusee() {
        assertThrows(IllegalArgumentException.class, () -> ValidationTransfert.normaliserDateNaissance(null));
        assertThrows(IllegalArgumentException.class, () -> ValidationTransfert.normaliserDateNaissance(" "));
        String demain = LocalDate.now(Clock.systemDefaultZone()).plusDays(1).toString();
        assertThrows(IllegalArgumentException.class, () -> ValidationTransfert.normaliserDateNaissance(demain));
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -150_000})
    void montantNulOuNegatifRefuse(long montant) {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationTransfert.valider(CLIENT, montant, PAYS));
    }

    @Test
    void nomEtPaysObligatoires() {
        assertThrows(IllegalArgumentException.class, () -> ValidationTransfert.valider(" ", 1000, PAYS));
        assertThrows(IllegalArgumentException.class, () -> ValidationTransfert.valider(null, 1000, PAYS));
        assertThrows(IllegalArgumentException.class, () -> ValidationTransfert.valider(CLIENT, 1000, ""));
    }

    @Test
    void saisieCorrecteAcceptee() {
        assertDoesNotThrow(() -> ValidationTransfert.valider(CLIENT, 1, PAYS));
    }
}
