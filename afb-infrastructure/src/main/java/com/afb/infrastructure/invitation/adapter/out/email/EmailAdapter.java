package com.afb.infrastructure.invitation.adapter.out.email;

import com.afb.domain.agent.exception.ServiceInvitationIndisponible;
import com.afb.domain.invitation.model.Invitation;
import com.afb.domain.invitation.port.out.EmailPort;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ADAPTATEUR DE SORTIE : envoi des emails par le SMTP de la plateforme.
 *
 * L'application envoie elle-même ses messages plutôt que de s'en remettre aux
 * templates de Keycloak : le contenu est en français, à la marque Afriland, et
 * le lien renvoie sur la plateforme et non sur une page Keycloak.
 */
@Component
public class EmailAdapter implements EmailPort {

    private static final Logger log = LoggerFactory.getLogger(EmailAdapter.class);

    private static final String CHEMIN_LOGO = "email/logo-afb.png";
    private static final String ID_LOGO = "logoAfb";

    // Palette reprise de styles.css du frontend, pour que l'email et la page
    // d'activation qu'il ouvre ne se contredisent pas visuellement.
    private static final String ROUGE = "#C8102E";
    private static final String NOIR = "#1A1D23";
    private static final String GRIS_TEXTE = "#5A6270";

    private final JavaMailSender expediteur;
    private final String adresseExpediteur;
    private final String nomExpediteur;

    public EmailAdapter(JavaMailSender expediteur,
                        @Value("${app.email.from:}") String adresseExpediteur,
                        @Value("${app.email.from-name:Afriland First Bank}") String nomExpediteur) {
        this.expediteur = expediteur;
        this.adresseExpediteur = adresseExpediteur;
        this.nomExpediteur = nomExpediteur;
    }

    /**
     * Ce qu'un rôle peut faire une fois connecté. Repris des menus réels de
     * l'application (espace administration et espace opérationnel), pour que
     * l'invitation ne promette rien qui n'existe pas.
     */
    private record Presentation(String titreAccent, String intro, List<String> fonctionnalites) {}

    private static final Presentation ADMIN = new Presentation(
            "espace administrateur",
            "Afriland First Bank vous ouvre un accès à sa plateforme de contrôle des plafonds "
            + "de transferts Hors CEMAC. En tant qu'administrateur de votre institution, "
            + "vous pilotez l'activité de vos équipes depuis un espace dédié.",
            List.of(
                "Gérer vos agents : créer leurs comptes, les activer ou les suspendre, "
                    + "et leur envoyer leur invitation",
                "Suivre les transferts de votre institution : vérification des plafonds, "
                    + "historique, annulations, dossiers non clôturés",
                "Consulter le tableau de bord, le bilan journalier et les statistiques "
                    + "de votre activité",
                "Gérer votre profil et votre mot de passe depuis les paramètres"));

    private static final Presentation AGENT = new Presentation(
            "compte agent",
            "Un compte vient d'être créé pour vous sur la plateforme de contrôle des plafonds "
            + "de transferts Hors CEMAC d'Afriland First Bank. Vous y traitez vos opérations "
            + "au quotidien.",
            List.of(
                "Vérifier le plafond mensuel d'un client avant d'exécuter un transfert",
                "Enregistrer un nouveau transfert et le clôturer",
                "Consulter votre historique et votre bilan journalier",
                "Annuler un transfert et suivre vos dossiers non clôturés"));

    private static Presentation presentation(String role) {
        return "ADMIN".equalsIgnoreCase(role) ? ADMIN : AGENT;
    }

    @Override
    public void envoyerInvitation(String destinataire, String nomComplet, String lien,
                                  String role, boolean renvoi) {
        Presentation quoi = presentation(role);
        String objet = renvoi
                ? "Votre nouveau lien d'accès à la plateforme de transferts"
                : ("ADMIN".equalsIgnoreCase(role)
                    ? "Votre espace administrateur sur la plateforme Afriland First Bank"
                    : "Votre accès agent à la plateforme de transferts Afriland First Bank");
        try {
            MimeMessage message = expediteur.createMimeMessage();
            // Multipart obligatoire pour porter les deux versions du corps
            // (texte de repli + HTML) dans le même message.
            MimeMessageHelper aide = new MimeMessageHelper(
                    message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());
            aide.setFrom(adresseExpediteur, nomExpediteur);
            aide.setTo(destinataire);
            aide.setSubject(objet);
            aide.setText(corpsTexte(nomComplet, lien, quoi, renvoi),
                         corpsHtml(nomComplet, lien, quoi, renvoi));

            // Logo embarqué dans le message : les clients de messagerie bloquent
            // presque tous les images distantes, un <img src="http…"> resterait vide.
            ClassPathResource logo = new ClassPathResource(CHEMIN_LOGO);
            if (logo.exists()) {
                aide.addInline(ID_LOGO, logo, "image/png");
            } else {
                log.warn("Logo {} absent du classpath : l'email partira sans en-tête illustré.",
                        CHEMIN_LOGO);
            }

            expediteur.send(message);
            log.info("Invitation envoyée à {}{}", destinataire, renvoi ? " (renvoi)" : "");
        } catch (MailException | jakarta.mail.MessagingException
                 | java.io.UnsupportedEncodingException e) {
            log.error("Échec de l'envoi de l'invitation à {}", destinataire, e);
            throw new ServiceInvitationIndisponible(
                    "L'email d'invitation n'a pas pu être envoyé à " + destinataire
                    + ". Vérifiez la configuration SMTP de la plateforme, puis réessayez.", e);
        }
    }

    /** Repli texte, pour les clients de messagerie qui n'affichent pas le HTML. */
    private String corpsTexte(String nomComplet, String lien, Presentation quoi, boolean renvoi) {
        StringBuilder sb = new StringBuilder("Bonjour ").append(nomComplet).append(",\n\n");
        if (renvoi) {
            sb.append("Voici votre nouveau lien d'accès. Le lien précédent n'est plus valable.\n\n");
        }
        sb.append(quoi.intro()).append("\n\n")
          .append("Une fois connecté, vous pourrez :\n");
        quoi.fonctionnalites().forEach(f -> sb.append("  - ").append(f).append('\n'));
        return sb.append("\nDéfinissez votre mot de passe ici :\n").append(lien).append("\n\n")
                .append("Ce lien est valable ").append(Invitation.VALIDITE_HEURES)
                .append(" heures et ne peut servir qu'une fois.\n\n")
                .append("Si vous n'attendiez pas ce message, ignorez-le.\n\n")
                .append("Afriland First Bank")
                .toString();
    }

    private String corpsHtml(String nomComplet, String lien, Presentation quoi, boolean renvoi) {
        String intro = renvoi
                ? "Voici votre nouveau lien d'accès — le lien précédent n'est plus valable. "
                  + quoi.intro()
                : quoi.intro();
        // Mise en page calquée sur la page d'activation (invitation.component.html) :
        // logo, titre à accent rouge, sous-titre, puis l'action. Tout en tables et
        // styles en ligne — les clients de messagerie ignorent flexbox et <style>.
        // Polices système : Manrope/Sora ne se chargent pas dans un email.
        return """
            <!doctype html>
            <html lang="fr"><body style="margin:0;padding:28px 16px;background:#F1F2F4;
                font-family:'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:%s;">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" border="0">
                <tr><td align="center">
                  <table role="presentation" width="560" cellpadding="0" cellspacing="0" border="0"
                         style="background:#FFFFFF;border-radius:16px;border:1px solid #E3E5E8;">

                    <tr><td align="center" style="padding:36px 36px 0;">
                      <img src="cid:%s" alt="Afriland First Bank" width="150"
                           style="display:block;border:0;max-width:150px;height:auto;">
                    </td></tr>

                    <tr><td align="center" style="padding:24px 36px 0;">
                      <div style="font-size:25px;font-weight:700;line-height:1.25;color:%s;">
                        Créez votre<br>
                        <span style="color:%s;">%s</span>
                      </div>
                      <div style="padding-top:10px;font-size:13.5px;color:%s;">
                        Plateforme de suivi des transferts internationaux
                      </div>
                    </td></tr>

                    <tr><td style="padding:28px 36px 0;">
                      <p style="margin:0 0 14px;font-size:15px;color:%s;">Bonjour %s,</p>
                      <p style="margin:0 0 20px;font-size:14px;line-height:1.65;color:%s;">%s</p>
                      <p style="margin:0 0 10px;font-size:13px;font-weight:700;color:%s;">
                        Une fois connecté, vous pourrez :
                      </p>
                      <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%"
                             style="margin:0 0 24px;">
                        %s
                      </table>
                    </td></tr>

                    <tr><td align="center" style="padding:0 36px;">
                      <table role="presentation" cellpadding="0" cellspacing="0" border="0" width="100%%">
                        <tr><td align="center" style="border-radius:9px;background:%s;">
                          <a href="%s" style="display:block;padding:14px 24px;font-size:14.5px;
                             font-weight:600;color:#FFFFFF;text-decoration:none;">
                            Définir mon mot de passe
                          </a>
                        </td></tr>
                      </table>
                    </td></tr>

                    <tr><td style="padding:22px 36px 0;">
                      <div style="border-left:3px solid %s;background:#FAFAFB;border-radius:0 8px 8px 0;
                                  padding:12px 14px;font-size:12.5px;line-height:1.6;color:%s;">
                        Ce lien est valable <strong>%d heures</strong> et ne peut servir qu'une fois.
                      </div>
                      <p style="margin:16px 0 0;font-size:12px;line-height:1.6;color:%s;">
                        Si le bouton ne fonctionne pas, copiez cette adresse dans votre navigateur :
                      </p>
                      <p style="margin:6px 0 0;font-size:11.5px;word-break:break-all;color:%s;">%s</p>
                    </td></tr>

                    <tr><td style="padding:26px 36px 28px;">
                      <div style="border-top:1px solid #ECEDEF;padding-top:16px;font-size:11.5px;
                                  line-height:1.6;color:#8A9099;">
                        Si vous n'attendiez pas ce message, vous pouvez l'ignorer.<br>
                        Message automatique, merci de ne pas y répondre.
                      </div>
                    </td></tr>

                  </table>
                  <div style="padding-top:16px;font-size:11px;color:#9AA1AD;">
                    © Afriland First Bank · Usage interne
                  </div>
                </td></tr>
              </table>
            </body></html>
            """.formatted(NOIR, ID_LOGO, NOIR, ROUGE, quoi.titreAccent(), GRIS_TEXTE, NOIR,
                          echapper(nomComplet), GRIS_TEXTE, intro, NOIR,
                          listeFonctionnalites(quoi), ROUGE, lien,
                          ROUGE, GRIS_TEXTE, Invitation.VALIDITE_HEURES,
                          GRIS_TEXTE, GRIS_TEXTE, lien);
    }

    /**
     * Les puces en lignes de tableau plutôt qu'en {@code <ul>} : les listes sont
     * rendues de façon très inégale d'un client de messagerie à l'autre.
     */
    private String listeFonctionnalites(Presentation quoi) {
        StringBuilder sb = new StringBuilder();
        for (String f : quoi.fonctionnalites()) {
            sb.append("""
                <tr>
                  <td width="18" valign="top" style="padding:0 0 9px;font-size:14px;
                      line-height:1.55;color:%s;">&bull;</td>
                  <td valign="top" style="padding:0 0 9px;font-size:13.5px;
                      line-height:1.55;color:%s;">%s</td>
                </tr>
                """.formatted(ROUGE, GRIS_TEXTE, echapper(f)));
        }
        return sb.toString();
    }

    /** Le nom vient d'une saisie admin : il ne doit pas pouvoir injecter de balise. */
    private static String echapper(String v) {
        return v == null ? "" : v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
