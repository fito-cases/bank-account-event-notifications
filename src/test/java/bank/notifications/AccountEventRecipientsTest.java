package bank.notifications;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class AccountEventRecipientsTest {
    private static final String ERROR = "Starter checks for qualifying recipients and threshold crossings must expose incomplete account-event notifications.";
    private static final AccountEvent.Recipient MANAGER = AccountEvent.Recipient.MANAGER;
    private static final AccountEvent.Recipient SECURITY = AccountEvent.Recipient.DIGITAL_SECURITY;
    private static final AccountEvent.Recipient INVESTMENT = AccountEvent.Recipient.INVESTMENT_MANAGEMENT;

    private static Account account(String balance) {
        return new Account("Ana Lima", "12345678900", "0102", "45678-9", new BigDecimal(balance));
    }

    private static void expect(List<AccountEvent> events, AccountEvent.Name... names) {
        if (events.size() != names.length) throw new AssertionError(ERROR);
        for (int i = 0; i < names.length; i++) {
            AccountEvent event = events.get(i);
            AccountEvent.Recipient recipient = switch (names[i]) {
                case OVERDRAFT_ENTRY, MANAGER_WITHDRAWAL -> MANAGER;
                case SECURITY_WITHDRAWAL -> SECURITY;
                case INVESTMENT_CROSSING -> INVESTMENT;
            };
            if (event.name() != names[i] || event.recipient() != recipient) throw new AssertionError(ERROR);
        }
    }

    private static List<AccountEvent> observe(Account account) {
        List<AccountEvent> events = new ArrayList<>();
        account.subscribe(events::add, Set.of(MANAGER, SECURITY, INVESTMENT));
        return events;
    }

    public static void main(String[] args) {
        for (TransactionKind kind : TransactionKind.values()) {
            Account account = account("0");
            List<AccountEvent> events = observe(account);
            account.withdraw(new BigDecimal("1"), kind);
            expect(events, AccountEvent.Name.OVERDRAFT_ENTRY);
            events.clear();
            account.withdraw(new BigDecimal("1"), kind);
            expect(events);

            account = account("10000");
            events = observe(account);
            account.withdraw(new BigDecimal("4999.99"), kind);
            expect(events);
            account.withdraw(new BigDecimal("5000"), kind);
            expect(events, AccountEvent.Name.MANAGER_WITHDRAWAL);

            account = account("20000");
            events = observe(account);
            account.withdraw(new BigDecimal("9999.99"), kind);
            expect(events, AccountEvent.Name.MANAGER_WITHDRAWAL);
            events.clear();
            account.withdraw(new BigDecimal("10000"), kind);
            expect(events, AccountEvent.Name.SECURITY_WITHDRAWAL);

            account = account("5000");
            events = observe(account);
            account.withdraw(new BigDecimal("10000"), kind);
            expect(events, AccountEvent.Name.OVERDRAFT_ENTRY, AccountEvent.Name.SECURITY_WITHDRAWAL);

            account = account("49999.99");
            events = observe(account);
            account.deposit(new BigDecimal("0.01"), kind);
            expect(events, AccountEvent.Name.INVESTMENT_CROSSING);
            events.clear();
            account.deposit(new BigDecimal("1"), kind);
            expect(events);

            account = account("49998");
            events = observe(account);
            account.deposit(new BigDecimal("1"), kind);
            expect(events);
            account.deposit(new BigDecimal("1"), kind);
            expect(events, AccountEvent.Name.INVESTMENT_CROSSING);

            account = account("50000");
            events = observe(account);
            account.deposit(new BigDecimal("1"), kind);
            expect(events);
            account.withdraw(new BigDecimal("2"), kind);
            expect(events);
            account.deposit(new BigDecimal("1"), kind);
            expect(events, AccountEvent.Name.INVESTMENT_CROSSING);
        }

        Account account = account("20000");
        List<AccountEvent> one = new ArrayList<>(), two = new ArrayList<>(), uninterested = new ArrayList<>();
        AccountObserver first = one::add;
        AccountObserver second = two::add;
        account.subscribe(first, Set.of(MANAGER));
        account.subscribe(second, Set.of(MANAGER));
        account.subscribe(uninterested::add, Set.of(INVESTMENT));
        account.withdraw(new BigDecimal("5000"), TransactionKind.PIX);
        expect(one, AccountEvent.Name.MANAGER_WITHDRAWAL);
        expect(two, AccountEvent.Name.MANAGER_WITHDRAWAL);
        expect(uninterested);
        account.unsubscribe(first);
        account.withdraw(new BigDecimal("5000"), TransactionKind.TRANSFER);
        expect(one, AccountEvent.Name.MANAGER_WITHDRAWAL);
        expect(two, AccountEvent.Name.MANAGER_WITHDRAWAL, AccountEvent.Name.MANAGER_WITHDRAWAL);
        expect(uninterested);
    }
}
