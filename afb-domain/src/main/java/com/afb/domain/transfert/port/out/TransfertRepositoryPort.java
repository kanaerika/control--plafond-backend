package com.afb.domain.transfert.port.out;

import com.afb.domain.transfert.model.Transfert;
import java.time.LocalDate;

public interface TransfertRepositoryPort {
    long cumulMensuel(String nomClient, String numeroPiece, String dateNaissance,
                      LocalDate debut, LocalDate fin);
    Transfert enregistrer(Transfert transfert);
    Transfert dernierTransfertDuClient(String nomClient);
}