package bank.notifications;

@FunctionalInterface
public interface AccountObserver {
    void onAccountEvent(AccountEvent event);
}
