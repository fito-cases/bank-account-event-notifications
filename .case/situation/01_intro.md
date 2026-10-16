# Ticket: account activity notifications

**From:** Banking operations
**To:** Account services team

Our current-account service needs to notify different teams when customer activity meets these rules:

- When a balance moves from nonnegative to negative, notify the account manager that the account has entered overdraft.
- For a withdrawal of at least R$5,000 but less than R$10,000, notify the account manager.
- For a withdrawal of R$10,000 or more, notify digital security.
- When an incoming transaction moves the balance from below R$50,000 to R$50,000 or above, notify investment management.

Payment, transfer, and Pix are transaction-kind inputs to withdrawal and deposit operations, not separate operations. Each message must include the event name, customer name and CPF, branch and account numbers, and the resulting balance. The account service must not send messages or depend on concrete sender classes. SMS and WhatsApp should be separate, testable in-process implementations; no external messaging integration is needed.

Please provide working Java code and tests for recipients, thresholds, message content, and separation of account activity from delivery.
