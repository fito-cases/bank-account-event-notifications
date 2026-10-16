package bank.notifications;

import java.util.ArrayList;
import java.util.List;

public final class SmsSender implements MessageSender {
    private final List<String> messages = new ArrayList<>();

    @Override public void record(AccountEvent.Recipient recipient, String message) {
        messages.add("SMS to " + recipient + ": " + message);
    }

    @Override public List<String> messages() { return List.copyOf(messages); }
}
