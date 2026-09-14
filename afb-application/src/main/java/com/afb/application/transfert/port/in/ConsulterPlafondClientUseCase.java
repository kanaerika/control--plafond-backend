package com.afb.application.transfert.port.in;

/** PORT D'ENTRÉE : contrôle en lecture seule du plafond déjà atteint par un client. */
public interface ConsulterPlafondClientUseCase {

    PlafondClientResultat pour(String nomClient, String numeroPiece, String dateNaissance);
}
