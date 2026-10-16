package bank.notifications;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class Account {
    private final String customerName;
    private final String cpf;
    private final String branch;
    private final String accountNumber;
    private BigDecimal balance;
    private final Map<AccountObserver, Set<AccountEvent.Recipient>> observers = new LinkedHashMap<>();

    public Account(String customerName, String cpf, String branch, String accountNumber, BigDecimal balance) {
        this.customerName = Objects.requireNonNull(customerName);
        this.cpf = Objects.requireNonNull(cpf);
        this.branch = Objects.requireNonNull(branch);
        this.accountNumber = Objects.requireNonNull(accountNumber);
        this.balance = Objects.requireNonNull(balance);
    }

    public BigDecimal balance() { return balance; }

    public void subscribe(AccountObserver observer, Set<AccountEvent.Recipient> recipients) {
        Objects.requireNonNull(observer);
        Objects.requireNonNull(recipients);
        if (recipients.isEmpty()) throw new IllegalArgumentException("At least one recipient required");
        observers.put(observer, Set.copyOf(recipients));
    }

    public void unsubscribe(AccountObserver observer) { observers.remove(observer); }

    public void withdraw(BigDecimal amount, TransactionKind kind) {
        validate(amount, kind);
        BigDecimal before = balance;
        balance = balance.subtract(amount);
        if (before.signum() >= 0 && balance.signum() < 0)
            publish(AccountEvent.Name.OVERDRAFT_ENTRY, AccountEvent.Recipient.MANAGER);
        if (amount.compareTo(new BigDecimal("5000")) >= 0 && amount.compareTo(new BigDecimal("10000")) < 0)
            publish(AccountEvent.Name.MANAGER_WITHDRAWAL, AccountEvent.Recipient.MANAGER);
        if (amount.compareTo(new BigDecimal("10000")) >= 0)
            publish(AccountEvent.Name.SECURITY_WITHDRAWAL, AccountEvent.Recipient.DIGITAL_SECURITY);
    }

    public void deposit(BigDecimal amount, TransactionKind kind) {
        validate(amount, kind);
        BigDecimal before = balance;
        balance = balance.add(amount);
        BigDecimal threshold = new BigDecimal("50000");
        if (before.compareTo(threshold) < 0 && balance.compareTo(threshold) >= 0)
            publish(AccountEvent.Name.INVESTMENT_CROSSING, AccountEvent.Recipient.INVESTMENT_MANAGEMENT);
    }

    private void publish(AccountEvent.Name name, AccountEvent.Recipient recipient) {
        AccountEvent event = new AccountEvent(name, recipient, customerName, cpf, branch, accountNumber, balance);
        for (var subscription : Map.copyOf(observers).entrySet()) {
            if (subscription.getValue().contains(recipient)) subscription.getKey().onAccountEvent(event);
        }
    }

    private static void validate(BigDecimal amount, TransactionKind kind) {
        Objects.requireNonNull(kind);
        Objects.requireNonNull(amount);
        if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
    }
}
