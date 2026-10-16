package bank.notifications;

import java.math.BigDecimal;
import java.util.List;

public final class NotificationPayloadTest {
    private static final String ERROR = "Starter checks for required message fields or sender independence must expose unfinished notification behavior.";

    private static void verify(String message, AccountEvent event) {
        for (String required : List.of(event.name().name(), event.recipient().name(), event.customerName(),
                event.cpf(), event.branch(), event.accountNumber(), event.balance().toPlainString())) {
            if (!message.contains(required)) throw new AssertionError(ERROR);
        }
    }

    public static void main(String[] args) {
        AccountEvent event = new AccountEvent(AccountEvent.Name.MANAGER_WITHDRAWAL,
                AccountEvent.Recipient.MANAGER, "Ana Lima", "12345678900", "0102", "45678-9", new BigDecimal("5000.00"));
        SmsSender sms = new SmsSender();
        WhatsAppSender whatsapp = new WhatsAppSender();
        new NotificationReaction(sms).onAccountEvent(event);
        new NotificationReaction(whatsapp).onAccountEvent(event);
        if (sms.messages().size() != 1 || whatsapp.messages().size() != 1) throw new AssertionError(ERROR);
        verify(sms.messages().getFirst(), event);
        verify(whatsapp.messages().getFirst(), event);
    }
}
