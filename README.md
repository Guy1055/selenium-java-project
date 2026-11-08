# Selenium Java QA Automation

End-to-end automation of [Toolshop](https://practicesoftwaretesting.com), a public demo store built for test practice: the customer journeys, the admin area, the whole REST API, and database checks.

It implements the same scenarios, with the same ids, as its sister project in Playwright + TypeScript: [playwright-project](https://github.com/Guy1055/playwright-project). The scenario list and the defects found in the site live in that project's [TEST-PLAN.md](https://github.com/Guy1055/playwright-project/blob/main/TEST-PLAN.md).

## Stack
Java 21, Maven Wrapper, Selenium 4, TestNG, REST Assured, JDBC (MariaDB), axe-core.

## What is covered
| Layer | Where | Notes |
|---|---|---|
| UI, customer | `tests/ui/` | Catalog, product, comparison, cart, checkout (5 payment methods, guest), auth, contact, account |
| UI, admin | `tests/ui/admin/` | Menu, reports, brands, categories, products, orders, users, messages |
| API | `tests/api/` | All 81 operations in the OpenAPI document, with permissions and validation |
| Database | `tests/db/`, `db/` | SQL against the real schema. **Placeholder tier**: the demo exposes no database, so these skip unless `DB_URL` is set |
| Quality | `tests/quality/` | axe-core accessibility, screenshot regression (local only) |

## Architecture
```
src/test/java/com/toolshop/
  core/    DriverFactory, Base tests, Expect (retrying assertions), @KnownBug, retry
  pages/   Page Objects (pages/admin/ for the back office)
  api/     ToolshopApi: thin REST Assured client
  db/      DbClient and the SQL queries
  data/    Test data
  tests/   Test classes by area
```

### Decisions worth explaining
- **`Expect` brings Playwright's web-first assertions to Selenium.** Every assertion retries until it holds or times out, so there are no sleeps. Page objects auto-wait the same way.
- **`@KnownBug` mirrors Playwright's `test.fail`.** The test describes correct behaviour; it passes while the site is broken and fails the day it is fixed. TestNG has no equivalent, so a small listener inverts the result.
- **The API is the source of truth.** Catalog data changes on a shared demo, so sort and filter tests compare the UI to the API at runtime.
- **Tests sign in by seeding the token,** so only the login tests pay for the login form.
- **Writes touch only records the test created,** with cleanup that runs even when the test fails. Negative login tests use throwaway users, because failures lock the shared demo accounts.
- **One browser per test, on a ThreadLocal,** so tests run in parallel without sharing state.

## Running
On Windows use `mvnw.cmd` in place of `./mvnw`. Chrome must be installed; Selenium Manager fetches the matching driver.
```bash
./mvnw test
```
```bash
./mvnw test -Dgroups=sanity
```
```bash
./mvnw test -Dgroups=api
```
```bash
./mvnw test -DexcludedGroups=visual
```

| Env var | Purpose |
|---|---|
| `HEADLESS` | Headless local run (always headless in CI) |
| `BASE_URL`, `API_URL` | Target another environment |
| `DB_URL` | Enable the SQL tier, e.g. `jdbc:mariadb://localhost:3306/toolshop?user=user&password=root` against a self-hosted Toolshop |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Override the public demo admin |
| `HTTPS_PROXY` | Corporate proxy |
| `UPDATE_SNAPSHOTS` | Rewrite the visual baselines |

## CI
[`.github/workflows/selenium.yml`](.github/workflows/selenium.yml) runs the `sanity` group on every push and the full suite nightly and on demand. The public site shows a Cloudflare check to data-center IPs, so CI starts a private copy of Toolshop in Docker and tests that, which also gives the SQL tier a real database. Surefire reports and failure screenshots are kept as artifacts.
