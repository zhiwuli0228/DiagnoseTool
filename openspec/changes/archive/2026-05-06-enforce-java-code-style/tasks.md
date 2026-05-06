## 1. Scope And Verification Baseline

- [x] 1.1 Inspect `docs/code-style.md` and confirm the exact copyright and Javadoc conventions to apply.
- [x] 1.2 Inventory maintained Java source files under `src/main/java` and decide whether any generated or test-only files are out of scope.
- [x] 1.3 Choose the least invasive verification approach for copyright headers and public Javadoc rules.

## 2. Copyright Headers

- [x] 2.1 Add the standard copyright header to Java source files that do not already have it.
- [x] 2.2 Ensure headers are placed before `package` declarations and are not duplicated.

## 3. Public Type Javadoc

- [x] 3.1 Add or normalize Javadoc for public classes, interfaces, enums, and records.
- [x] 3.2 Include meaningful summaries and required metadata such as author and since where applicable.

## 4. Public Method Javadoc

- [x] 4.1 Add or normalize Javadoc for public methods and constructors in maintained Java source.
- [x] 4.2 Ensure every public method parameter has a non-empty `@param` description.
- [x] 4.3 Ensure non-void public methods have non-empty `@return` descriptions.
- [x] 4.4 Ensure documented thrown exceptions use valid `@throws` tags with non-empty descriptions.
- [x] 4.5 Remove invalid Javadoc tags and avoid noisy boilerplate.

## 5. Verification

- [x] 5.1 Add or update repeatable verification for the Java copyright and public Javadoc requirements.
- [x] 5.2 Run the verification and fix any reported Java style issues.
- [x] 5.3 Run `mvn test`.
- [x] 5.4 Run `openspec status --change enforce-java-code-style` and confirm artifacts/tasks are ready.
