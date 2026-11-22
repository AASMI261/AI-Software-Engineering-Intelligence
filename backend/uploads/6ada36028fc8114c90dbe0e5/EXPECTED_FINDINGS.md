# Expected findings

A code review may identify these intentional examples:
1. `Calculator.java`: division by zero is not handled.
2. `Calculator.java`: `addOne()` adds 2 rather than 1; the test exposes this.
3. `UserService.java`: `displayName()` may throw NullPointerException for null input.
4. `UserService.java`: `isAdmin()` may throw NullPointerException for null input.
5. `FileReaderService.java`: file path is not validated; severity depends on usage.

These are test prompts, not a guarantee that DevRecovery detects them. If ZIP upload/import is not implemented yet, this archive is a fixture to use after that feature is available.
