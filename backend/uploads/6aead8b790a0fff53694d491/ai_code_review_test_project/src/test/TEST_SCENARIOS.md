# Suggested verification scenarios

After the AI proposes and applies fixes, verify these behaviors:

1. SQL query: input containing a quote should be handled as a parameter, not concatenated into SQL.
2. Authentication: no production password should be hardcoded; password verification should use a secure hash verifier.
3. File access: reject names that resolve outside the `uploads` directory.
4. Discount calculator: handle `null` explicitly and test values 99.99, 100.00, and 100.01 against the intended business rule.
5. HTML rendering: encode untrusted display names before inserting them into HTML.

## Approval workflow checks
- Findings should include file path, line, severity, explanation, and recommendation.
- Proposed changes should be shown as a diff before application.
- No source file should change before explicit user approval.
- After approval, run build/tests and report pass/fail output.
- If verification fails, explain the failure and do not claim the patch is verified.
