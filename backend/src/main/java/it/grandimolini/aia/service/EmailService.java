package it.grandimolini.aia.service;

import it.grandimolini.aia.model.Scadenza;
import it.grandimolini.aia.model.ProcessoDocumento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@grandimolini.it}")
    private String fromEmail;

    @Value("${aia.notifications.enabled:false}")
    private boolean notificationsEnabled;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void sendScadenzaNotification(Scadenza scadenza) {
        if (!notificationsEnabled || mailSender == null) {
            System.out.println("⚠️ Email notifications disabled. Would send email for: " + scadenza.getTitolo());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(scadenza.getEmailNotifica() != null ? scadenza.getEmailNotifica() : scadenza.getResponsabile() + "@grandimolini.it");
            message.setSubject("⏰ Promemoria Scadenza AIA: " + scadenza.getTitolo());

            String body = buildScadenzaEmailBody(scadenza);
            message.setText(body);

            mailSender.send(message);
            System.out.println("✅ Email inviata per scadenza: " + scadenza.getTitolo());
        } catch (Exception e) {
            System.err.println("❌ Errore invio email per scadenza " + scadenza.getTitolo() + ": " + e.getMessage());
        }
    }

    public void sendMultipleScadenzeNotification(List<Scadenza> scadenze, String recipientEmail) {
        if (!notificationsEnabled || mailSender == null || scadenze.isEmpty()) {
            System.out.println("⚠️ Email notifications disabled or no scadenze");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject("⏰ Riepilogo Scadenze AIA Imminenti (" + scadenze.size() + ")");

            StringBuilder body = new StringBuilder();
            body.append("Gentile responsabile,\n\n");
            body.append("Le seguenti scadenze AIA sono imminenti:\n\n");

            for (Scadenza scadenza : scadenze) {
                body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                body.append("📌 ").append(scadenza.getTitolo()).append("\n");
                body.append("   Stabilimento: ").append(scadenza.getStabilimento().getNome()).append("\n");
                body.append("   Data Scadenza: ").append(scadenza.getDataScadenza().format(DATE_FORMATTER)).append("\n");
                body.append("   Tipo: ").append(scadenza.getTipoScadenza()).append("\n");
                body.append("   Priorità: ").append(scadenza.getPriorita() != null ? scadenza.getPriorita() : "N/A").append("\n");
                if (scadenza.getNote() != null) {
                    body.append("   Note: ").append(scadenza.getNote()).append("\n");
                }
                body.append("\n");
            }

            body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            body.append("Si prega di prendere in carico le scadenze entro i termini previsti.\n\n");
            body.append("Cordiali saluti,\n");
            body.append("Sistema AIA Management - Grandi Molini Italiani\n");

            message.setText(body.toString());
            mailSender.send(message);
            System.out.println("✅ Email riepilogo inviata con " + scadenze.size() + " scadenze");
        } catch (Exception e) {
            System.err.println("❌ Errore invio email riepilogo: " + e.getMessage());
        }
    }

    private String buildScadenzaEmailBody(Scadenza scadenza) {
        StringBuilder body = new StringBuilder();
        body.append("Gentile ").append(scadenza.getResponsabile()).append(",\n\n");
        body.append("Ti ricordiamo la seguente scadenza AIA imminente:\n\n");
        body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        body.append("📌 Titolo: ").append(scadenza.getTitolo()).append("\n");
        body.append("🏭 Stabilimento: ").append(scadenza.getStabilimento().getNome()).append(" - ").append(scadenza.getStabilimento().getCitta()).append("\n");
        body.append("📅 Data Scadenza: ").append(scadenza.getDataScadenza().format(DATE_FORMATTER)).append("\n");
        body.append("🔔 Tipo: ").append(scadenza.getTipoScadenza()).append("\n");
        body.append("⚠️ Priorità: ").append(scadenza.getPriorita() != null ? scadenza.getPriorita() : "N/A").append("\n");

        if (scadenza.getNote() != null && !scadenza.getNote().isEmpty()) {
            body.append("\n📝 Note:\n").append(scadenza.getNote()).append("\n");
        }

        body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        long giorniRimanenti = java.time.temporal.ChronoUnit.DAYS.between(
            java.time.LocalDate.now(),
            scadenza.getDataScadenza()
        );

        if (giorniRimanenti <= 0) {
            body.append("⚠️ ATTENZIONE: La scadenza è già trascorsa o scade oggi!\n\n");
        } else if (giorniRimanenti <= 7) {
            body.append("⏰ URGENTE: Mancano solo ").append(giorniRimanenti).append(" giorni alla scadenza!\n\n");
        } else {
            body.append("ℹ️ Mancano ").append(giorniRimanenti).append(" giorni alla scadenza.\n\n");
        }

        body.append("Si prega di prendere in carico questa scadenza quanto prima.\n\n");
        body.append("Cordiali saluti,\n");
        body.append("Sistema AIA Management - Grandi Molini Italiani\n\n");
        body.append("--\n");
        body.append("Questa è una email automatica, si prega di non rispondere.\n");

        return body.toString();
    }

    public void sendNonConformitaAlert(String recipientEmail, String parametro, Double valore, Double limite, String stabilimento) {
        if (!notificationsEnabled || mailSender == null) {
            System.out.println("⚠️ Email notifications disabled. Would send non-conformità alert");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject("🚨 ALERT: Non Conformità Ambientale - " + stabilimento);

            StringBuilder body = new StringBuilder();
            body.append("ALERT NON CONFORMITÀ AMBIENTALE\n\n");
            body.append("Stabilimento: ").append(stabilimento).append("\n");
            body.append("Parametro: ").append(parametro).append("\n");
            body.append("Valore Misurato: ").append(valore).append("\n");
            body.append("Limite Autorizzato: ").append(limite).append("\n");
            body.append("Superamento: ").append("%.2f%%".formatted((valore / limite - 1) * 100)).append("\n\n");
            body.append("Si richiede intervento immediato.\n\n");
            body.append("Sistema AIA Management - Grandi Molini Italiani\n");

            message.setText(body.toString());
            mailSender.send(message);
            System.out.println("✅ Alert non-conformità inviato");
        } catch (Exception e) {
            System.err.println("❌ Errore invio alert non-conformità: " + e.getMessage());
        }
    }

    /**
     * Invia email per task di processo BPM.
     */
    public void sendProcessoTaskEmail(String to, String subject, String body, ProcessoDocumento processo) {
        if (!notificationsEnabled || mailSender == null) {
            log.info("EMAIL (mock) → To: {} | Subject: {} | Body: {}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(to);
            msg.setSubject(subject != null ? subject : "Notifica processo " + processo.getCodiceProcesso());
            msg.setText(body != null ? body : "Il processo " + processo.getCodiceProcesso() + " ha raggiunto un nuovo step.");
            mailSender.send(msg);
            log.info("Email inviata a {} per processo {}", to, processo.getCodiceProcesso());
        } catch (Exception e) {
            log.error("Errore invio email BPM: {}", e.getMessage());
        }
    }
}
