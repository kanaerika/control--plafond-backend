package com.afb.application.transfert.port.in;

import java.util.List;

public interface RechercherClientsUseCase {
    /** Auto-complétion : clients connus dont le nom contient la saisie (min. 2 caractères). */
    List<ClientConnuResultat> clientsConnus(String prefixe);
}