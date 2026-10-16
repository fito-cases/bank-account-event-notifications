package bank.notifications;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.Set;
import java.util.List;

public final class AccountIsolationTest {
    public static void main(String[] args) {
        for (Field field : Account.class.getDeclaredFields()) reject(field.getType());
        for (Method method : Account.class.getDeclaredMethods()) {
            reject(method.getReturnType());
            for (Class<?> parameter : method.getParameterTypes()) reject(parameter);
        }
        for (Constructor<?> constructor : Account.class.getDeclaredConstructors())
            for (Class<?> parameter : constructor.getParameterTypes()) reject(parameter);

        exercise(true, false);
        exercise(false, true);
        exercise(true, true);
    }

    private static void exercise(boolean useSms, boolean useWhatsApp) {
        Account account = new Account("Ana", "123", "001", "002", new BigDecimal("6000"));
        SmsSender sms = new SmsSender();
        WhatsAppSender whatsapp = new WhatsAppSender();
        Set<AccountEvent.Recipient> roles = Set.of(AccountEvent.Recipient.MANAGER,
                AccountEvent.Recipient.DIGITAL_SECURITY, AccountEvent.Recipient.INVESTMENT_MANAGEMENT);
        if (useSms) account.subscribe(new NotificationReaction(sms), roles);
        if (useWhatsApp) account.subscribe(new NotificationReaction(whatsapp), roles);
        account.withdraw(new BigDecimal("5000"), TransactionKind.PAYMENT);
        account.withdraw(new BigDecimal("10000"), TransactionKind.TRANSFER);
        account.deposit(new BigDecimal("59000"), TransactionKind.PIX);
        check(sms.messages(), useSms, "SMS");
        check(whatsapp.messages(), useWhatsApp, "WhatsApp");
    }

    private static void check(List<String> messages, boolean enabled, String channel) {
        if (!enabled) {
            if (!messages.isEmpty()) throw new AssertionError(channel + " was not selected");
            return;
        }
        if (messages.size() != 4 || messages.stream().anyMatch(m -> !m.startsWith(channel))
                || messages.stream().filter(m -> m.contains("to MANAGER:")).count() != 2
                || messages.stream().filter(m -> m.contains("to DIGITAL_SECURITY:")).count() != 1
                || messages.stream().filter(m -> m.contains("to INVESTMENT_MANAGEMENT:")).count() != 1)
            throw new AssertionError(channel + " routing/recording");
    }

    private static void reject(Class<?> type) {
        if (type == SmsSender.class || type == WhatsAppSender.class || type == NotificationReaction.class
                || type == MessageSender.class) throw new AssertionError("Account depends on delivery implementation");
    }
}
