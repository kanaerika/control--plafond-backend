package com.afb.infrastructure.partenaire.adapter.in.web;

import com.afb.application.partenaire.port.in.GererPartenairesUseCase;
import com.afb.application.partenaire.port.in.ModifierPartenaireCommande;
import com.afb.application.partenaire.port.in.PartenaireResultat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/partenaires")
@PreAuthorize("hasRole('ADMIN')")
public class PartenaireController {

    private final GererPartenairesUseCase gererPartenaires;

    public PartenaireController(GererPartenairesUseCase gererPartenaires) {
        this.gererPartenaires = gererPartenaires;
    }

    @GetMapping
    public List<PartenaireResultat> lister() {
        return gererPartenaires.lister();
    }

    @GetMapping("/{id}")
    public PartenaireResultat detail(@PathVariable Long id) {
        return gererPartenaires.detail(id);
    }

    @PutMapping("/{id}")
    public PartenaireResultat modifier(@PathVariable Long id,
                                       @RequestBody ModifierPartenaireRequete requete) {
        return gererPartenaires.modifier(
                new ModifierPartenaireCommande(id, requete.nom(), requete.email()));
    }

    @PatchMapping("/{id}/activation")
    public PartenaireResultat basculerActivation(@PathVariable Long id) {
        return gererPartenaires.basculerActivation(id);
    }

    @PostMapping("/{id}/invitation")
    public Map<String, String> renvoyerInvitation(@PathVariable Long id) {
        return Map.of("message", gererPartenaires.renvoyerInvitation(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        gererPartenaires.supprimer(id);
    }

    @PostMapping
public PartenaireResultat creer(@RequestBody CreerPartenaireRequete r) {
    return gererPartenaires.creer(new com.afb.application.partenaire.port.in.CreerPartenaireCommande(
            r.nom(), r.email(), r.nomAdministrateur()));
}

    public record CreerPartenaireRequete(String nom, String email, String nomAdministrateur) {}
    public record ModifierPartenaireRequete(String nom, String email) {}
}