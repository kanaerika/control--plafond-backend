package com.afb.application.transfert.port.in;

public record CloturerCommande(
        Long transfertId,
        String reference,
        String canal) {
}