package bank.notifications;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;

public final class AccountTransactionTest {
    public static void main(String[] args) {
        for (TransactionKind kind : TransactionKind.values()) {
            Account account = new Account("Ana", "123", "001", "002", new BigDecimal("12.30"));
            account.deposit(new BigDecimal("5.20"), kind);
            account.withdraw(new BigDecimal("7.10"), kind);
            if (account.balance().compareTo(new BigDecimal("10.40")) != 0) throw new AssertionError("Balance for " + kind);
        }
        for (String name : new String[]{"withdraw", "deposit"}) {
            try {
                Method method = Account.class.getMethod(name, BigDecimal.class, TransactionKind.class);
                if (method.getReturnType() != void.class) throw new AssertionError(name);
            } catch (NoSuchMethodException e) { throw new AssertionError(name, e); }
        }
        if (Arrays.stream(Account.class.getDeclaredMethods()).anyMatch(m ->
                Arrays.asList("pay", "transfer", "pix").contains(m.getName().toLowerCase())))
            throw new AssertionError("Transaction kinds must not be account methods");
    }
}
