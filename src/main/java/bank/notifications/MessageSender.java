package bank.notifications;

import java.util.List;

public interface MessageSender {
    void record(AccountEvent.Recipient recipient, String message);
    List<String> messages();
}
