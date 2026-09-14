package com.afb.application.partenaire.port.in;

import java.util.List;

/** PORT D'ENTRÉE : ce que l'extérieur peut demander concernant les partenaires. */
public interface GererPartenairesUseCase {

    List<PartenaireResultat> lister();
    PartenaireResultat detail(Long id);
    PartenaireResultat creer(CreerPartenaireCommande commande);
    PartenaireResultat modifier(ModifierPartenaireCommande commande);
    PartenaireResultat basculerActivation(Long id);
    void supprimer(Long id);

    /** Renvoie l'email d'invitation à l'administrateur du partenaire. */
    String renvoyerInvitation(Long id);
}