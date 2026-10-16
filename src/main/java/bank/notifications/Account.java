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
        balance = balance.subtract(amount);
    }

    public void deposit(BigDecimal amount, TransactionKind kind) {
        validate(amount, kind);
        balance = balance.add(amount);
    }

    private static void validate(BigDecimal amount, TransactionKind kind) {
        Objects.requireNonNull(kind);
        Objects.requireNonNull(amount);
        if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
    }
}
