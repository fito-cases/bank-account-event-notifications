package bank.notifications;

import java.math.BigDecimal;
import java.util.Objects;

public record AccountEvent(Name name, Recipient recipient, String customerName, String cpf,
                           String branch, String accountNumber, BigDecimal balance) {
    public enum Name { OVERDRAFT_ENTRY, MANAGER_WITHDRAWAL, SECURITY_WITHDRAWAL, INVESTMENT_CROSSING }
    public enum Recipient { MANAGER, DIGITAL_SECURITY, INVESTMENT_MANAGEMENT }

    public AccountEvent {
        Objects.requireNonNull(name);
        Objects.requireNonNull(recipient);
        Objects.requireNonNull(customerName);
        Objects.requireNonNull(cpf);
        Objects.requireNonNull(branch);
        Objects.requireNonNull(accountNumber);
        Objects.requireNonNull(balance);
    }
}
