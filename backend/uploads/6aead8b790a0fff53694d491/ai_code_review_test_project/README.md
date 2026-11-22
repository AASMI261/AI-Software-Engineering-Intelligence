# AI Code Review Test Project

This is a deliberately flawed Java sample project for testing your ZIP-analysis workflow.

## How to use
1. Upload `ai_code_review_test_project.zip` to your analyzer.
2. Ask it to inspect security, correctness, and maintainability.
3. Review the findings and proposed patches.
4. Approve only selected changes.
5. Confirm the tool applies the approved patch, shows a diff, runs verification, and reports any remaining issues.

## Expected findings (for evaluator use)
- `src/main/java/demo/UserRepository.java`: SQL injection risk from string-concatenated SQL.
- `src/main/java/demo/AuthService.java`: hardcoded demo credential and plaintext password comparison.
- `src/main/java/demo/FileService.java`: path traversal risk because the requested filename is not constrained to the storage directory.
- `src/main/java/demo/DiscountCalculator.java`: incorrect boundary/discount logic and possible null handling issue.
- `src/main/java/demo/HtmlRenderer.java`: unescaped user input inserted into HTML (XSS risk if rendered by a browser).

All credentials and data in this project are fake placeholders. Do not use this sample in production.
