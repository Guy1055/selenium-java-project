# Project: Selenium Java QA automation

Portfolio project. The same scenarios, with the same ids, as `Guy1055/playwright-project`.

## System under test
https://practicesoftwaretesting.com (Toolshop, a public demo app built for test practice).
- Shared public site: writes only on records the test created, then clean up. Never modify or delete seed data, and never call the database reset endpoint (`/refresh`).
- Never run negative login tests against the shared demo accounts (`admin@`, `customer@`): repeated failures lock them. Use a user the test registered.
- Use test accounts only; never real personal data.

## Conventions
- Java 21, Maven Wrapper, Selenium 4, TestNG, REST Assured.
- Page Object Model under `pages/` with PageFactory: elements are `@FindBy` fields on `data-test`, never `@CacheLookup`. Use `By` (`BasePage.testId`) only for locators built at run time or searched inside another element.
- No `Thread.sleep`. Page objects auto-wait; assertions go through `Expect`.
- Known site defects use `@KnownBug` with a BUG id from the Playwright project's TEST-PLAN.md.
- A new scenario goes into both projects under the same id.

## Git workflow (solo personal project)
- Work directly on `main`. Commit and push small working pieces without asking.
- Stage files by explicit path. Commit messages match the Playwright project (`type: summary`, body, `Commit #N`).
- Never force-push.

## Language
User is Hebrew-speaking: explain in Hebrew, keep code and identifiers in English.
