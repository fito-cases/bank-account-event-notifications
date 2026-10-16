# Intended approach (evaluator only)

Build an account publisher with a shared observer callback, registration, and event context carrying the event name and customer/account identifiers plus the resulting balance. Account operations detect transitions using balances before and after each operation: overdraft only on nonnegative-to-negative withdrawal; manager for withdrawals in [R$5,000, R$10,000), security for withdrawals >= R$10,000; investment management only for incoming below-to-at-least-R$50,000 crossings. Payment, transfer, and Pix are transaction kinds supplied to common withdrawal/deposit operations. Let independently registered observer reactions route the appropriate events to the intended recipients. Distinct SMS and WhatsApp in-process implementations format/send recordable messages; keep sender implementation dependencies out of the account. Tests should exercise exact boundaries, non-events, payload fields, and absence of concrete delivery dependencies.

# Rubric evidence

- **observer-collaboration:** Locate registration and publication through a shared observer contract; test dispatch from withdrawals and incoming transactions, including multiple listeners.
- **sender-decoupling:** Inspect imports, fields, constructors, and calls in account code: no concrete SMS/WhatsApp or delivery API. A newly attached sender should require no account edits.
- **event-recipients:** Check each of the four rules, recipient assignments, threshold equality, transition direction, and no repeated alerts when already beyond a crossing threshold. A large withdrawal may also enter overdraft; assess applicable events independently.
- **notification-context:** Check event name, customer name, CPF, branch, account number, and post-operation balance in messages for all applicable events.
- **distinct-message-reactions:** Check separate testable SMS and WhatsApp in-process implementations outside the account, selectable/combinable without changing it.

# Post-source review

On `main`, `./mvnw test -q` exits 1: `AccountEventRecipientsTest` and `NotificationPayloadTest` fail; `AccountIsolationTest` and `AccountTransactionTest` pass. `Account.withdraw` and `Account.deposit` change the balance but do not publish events to registered observers. `NotificationReaction.onAccountEvent` currently records the event name and customer name without CPF, branch, account number, or resulting balance. The scaffold already has `AccountEvent`, `AccountObserver`, `MessageSender`, in-process `SmsSender`/`WhatsAppSender`, and the contract suite in `pom.xml`. The exemplar is on `case/solution-v1.0.0`; use the actual code and test results there when scoring, rather than assuming that the starter's failures describe the exemplar.
