package bank.notifications;

public final class ContractSuiteRunner {
    public static void main(String[] args) {
        int failures = 0;
        Class<?>[] suite = {AccountEventRecipientsTest.class, NotificationPayloadTest.class,
                AccountIsolationTest.class, AccountTransactionTest.class};
        for (Class<?> test : suite) {
            try {
                test.getMethod("main", String[].class).invoke(null, (Object) new String[0]);
                System.out.println("PASS " + test.getSimpleName());
            } catch (ReflectiveOperationException e) {
                Throwable failure = e.getCause() == null ? e : e.getCause();
                System.out.println("FAIL " + test.getSimpleName() + ": " + failure.getMessage());
                failures++;
            } catch (Throwable e) {
                System.out.println("FAIL " + test.getSimpleName() + ": " + e.getMessage());
                failures++;
            }
        }
        System.out.println("Executed " + suite.length + " test classes; failures: " + failures);
        if (failures > 0) System.exit(1);
    }
}
