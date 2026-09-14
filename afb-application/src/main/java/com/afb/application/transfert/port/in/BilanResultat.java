package com.afb.application.transfert.port.in;

import java.time.LocalDate;
import java.util.List;

/** Bilan du jour : totaux + lignes détaillées. */
public record BilanResultat(
        LocalDate jour,
        int executes,
        int rejetes,
        int annules,
        int nonClotures,
        int total,
        List<TransfertResultat> lignes) {
}