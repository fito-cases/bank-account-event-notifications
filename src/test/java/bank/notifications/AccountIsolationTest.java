package bank.notifications;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.util.Set;

public final class AccountIsolationTest {
    public static void main(String[] args) {
        for (Field field : Account.class.getDeclaredFields()) reject(field.getType());
        for (Method method : Account.class.getDeclaredMethods()) {
            reject(method.getReturnType());
            for (Class<?> parameter : method.getParameterTypes()) reject(parameter);
        }
        for (Constructor<?> constructor : Account.class.getDeclaredConstructors())
            for (Class<?> parameter : constructor.getParameterTypes()) reject(parameter);

        Account account = new Account("Ana", "123", "001", "002", new BigDecimal("100"));
        SmsSender sms = new SmsSender();
        WhatsAppSender whatsapp = new WhatsAppSender();
        account.subscribe(new NotificationReaction(sms), Set.of(AccountEvent.Recipient.MANAGER));
        account.subscribe(new NotificationReaction(whatsapp), Set.of(AccountEvent.Recipient.MANAGER));
        AccountEvent context = new AccountEvent(AccountEvent.Name.MANAGER_WITHDRAWAL,
                AccountEvent.Recipient.MANAGER, "Ana", "123", "001", "002", BigDecimal.ZERO);
        new NotificationReaction(sms).onAccountEvent(context);
        new NotificationReaction(whatsapp).onAccountEvent(context);
        if (sms.messages().size() != 1 || whatsapp.messages().size() != 1
                || sms.messages().getFirst().equals(whatsapp.messages().getFirst()))
            throw new AssertionError("Independent in-process senders");
    }

    private static void reject(Class<?> type) {
        if (type == SmsSender.class || type == WhatsAppSender.class || type == NotificationReaction.class
                || type == MessageSender.class) throw new AssertionError("Account depends on delivery implementation");
    }
}
