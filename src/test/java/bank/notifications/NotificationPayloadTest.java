package bank.notifications;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public final class NotificationPayloadTest {
    private static final String ERROR = "Starter checks for required message fields or sender independence must expose unfinished notification behavior.";

    private static void verify(String message, AccountEvent event) {
        for (String required : List.of(event.name().name(), event.recipient().name(), event.customerName(),
                event.cpf(), event.branch(), event.accountNumber(), event.balance().toPlainString())) {
            if (!message.contains(required)) throw new AssertionError(ERROR);
        }
    }

    private static Account account(String balance) {
        return new Account("Ana Lima", "12345678900", "0102", "45678-9", new BigDecimal(balance));
    }

    private static void assertEvent(Account account, BigDecimal amount, boolean withdrawal,
                                    AccountEvent.Name name, AccountEvent.Recipient recipient, String result) {
        List<AccountEvent> observed = new ArrayList<>();
        account.subscribe(observed::add, Set.of(recipient));
        if (withdrawal) account.withdraw(amount, TransactionKind.PIX);
        else account.deposit(amount, TransactionKind.TRANSFER);
        if (observed.size() != 1 || observed.getFirst().name() != name
                || observed.getFirst().recipient() != recipient
                || observed.getFirst().balance().compareTo(new BigDecimal(result)) != 0)
            throw new AssertionError(ERROR);
        AccountEvent event = observed.getFirst();
        SmsSender sms = new SmsSender();
        WhatsAppSender whatsapp = new WhatsAppSender();
        new NotificationReaction(sms).onAccountEvent(event);
        new NotificationReaction(whatsapp).onAccountEvent(event);
        if (sms.messages().size() != 1 || whatsapp.messages().size() != 1
                || !sms.messages().getFirst().startsWith("SMS")
                || !whatsapp.messages().getFirst().startsWith("WhatsApp")) throw new AssertionError(ERROR);
        verify(sms.messages().getFirst(), event);
        verify(whatsapp.messages().getFirst(), event);
    }

    public static void main(String[] args) {
        assertEvent(account("0"), new BigDecimal("1"), true,
                AccountEvent.Name.OVERDRAFT_ENTRY, AccountEvent.Recipient.MANAGER, "-1");
        assertEvent(account("10000"), new BigDecimal("5000"), true,
                AccountEvent.Name.MANAGER_WITHDRAWAL, AccountEvent.Recipient.MANAGER, "5000");
        assertEvent(account("20000"), new BigDecimal("10000"), true,
                AccountEvent.Name.SECURITY_WITHDRAWAL, AccountEvent.Recipient.DIGITAL_SECURITY, "10000");
        assertEvent(account("49999"), new BigDecimal("1"), false,
                AccountEvent.Name.INVESTMENT_CROSSING, AccountEvent.Recipient.INVESTMENT_MANAGEMENT, "50000");
    }
}
