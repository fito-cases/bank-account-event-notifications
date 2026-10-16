package bank.notifications;

import java.util.Objects;

public final class NotificationReaction implements AccountObserver {
    private final MessageSender sender;

    public NotificationReaction(MessageSender sender) { this.sender = Objects.requireNonNull(sender); }

    @Override public void onAccountEvent(AccountEvent event) {
        sender.record(event.recipient(), "Event: " + event.name() + "; customer: " + event.customerName());
    }
}
