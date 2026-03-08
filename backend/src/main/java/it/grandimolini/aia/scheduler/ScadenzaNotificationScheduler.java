package it.grandimolini.aia.scheduler;

import it.grandimolini.aia.model.Scadenza;
import it.grandimolini.aia.service.EmailService;
import it.grandimolini.aia.service.ScadenzaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ScadenzaNotificationScheduler {

    @Autowired
    private ScadenzaService scadenzaService;

    @Autowired
    private EmailService emailService;

    @Value("${aia.notifications.enabled:false}")
    private boolean notificationsEnabled;

    @Value("${aia.notifications.default-recipient:admin@grandimolini.it}")
    private String defaultRecipient;

    // Esegui ogni giorno alle 9:00
    @Scheduled(cron = "0 0 9 * * ?")
    public void checkAndNotifyScadenze() {
        if (!notificationsEnabled) {
            System.out.println("ℹ️ Notifiche email disabilitate. Scheduler non attivo.");
            return;
        }

        System.out.println("🔔 Controllo scadenze imminenti...");

        try {
            // Trova scadenze nei prossimi 7 giorni
            List<Scadenza> scadenzeImminenti = scadenzaService.findScadenzeImminenti(7);

            if (scadenzeImminenti.isEmpty()) {
                System.out.println("✅ Nessuna scadenza imminente trovata");
                return;
            }

            System.out.println("⏰ Trovate " + scadenzeImminenti.size() + " scadenze imminenti");

            // Invia notifiche individuali per scadenze urgenti (entro 3 giorni)
            LocalDate now = LocalDate.now();
            for (Scadenza scadenza : scadenzeImminenti) {
                long giorniRimanenti = java.time.temporal.ChronoUnit.DAYS.between(now, scadenza.getDataScadenza());

                if (giorniRimanenti <= 3 && scadenza.getStato() != Scadenza.StatoScadenza.COMPLETATA) {
                    emailService.sendScadenzaNotification(scadenza);
                }
            }

            // Invia riepilogo giornaliero
            emailService.sendMultipleScadenzeNotification(scadenzeImminenti, defaultRecipient);

        } catch (Exception e) {
            System.err.println("❌ Errore durante il controllo scadenze: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Esegui ogni lunedì alle 8:00 per riepilogo settimanale
    @Scheduled(cron = "0 0 8 ? * MON")
    public void sendWeeklyScadenzeReport() {
        if (!notificationsEnabled) {
            return;
        }

        System.out.println("📊 Generazione report settimanale scadenze...");

        try {
            List<Scadenza> scadenze30Giorni = scadenzaService.findScadenzeProssimi30Giorni();

            if (!scadenze30Giorni.isEmpty()) {
                emailService.sendMultipleScadenzeNotification(scadenze30Giorni, defaultRecipient);
                System.out.println("✅ Report settimanale inviato con " + scadenze30Giorni.size() + " scadenze");
            }

        } catch (Exception e) {
            System.err.println("❌ Errore durante invio report settimanale: " + e.getMessage());
        }
    }
}
