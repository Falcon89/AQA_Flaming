<<<<<<< HEAD
# QA Automation Test Suite

Vasyl Kachala

API and UI automation for the Flamingo AQA home assignment.

| Area | Target |
|------|--------|
| REST | https://restful-booker.herokuapp.com |
| GraphQL | Hygraph Video schema (public playground) |
| UI | https://demoqa.com (Practice Form + Web Tables) |

## Prerequisites

- Java 17+
- Maven 3.6+
- Chrome / Chromium (Playwright downloads its own browser on first install)

## How to Run

```bash
# one-time browser install
mvn exec:java -e -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"

# everything
mvn clean test

# API only
mvn test "-Dgroups=api"

# UI only
mvn test "-Dgroups=ui"

# Surefire HTML report (after a test run)
mvn surefire-report:report-only

# Allure (optional)
mvn allure:serve
```

On Windows PowerShell put quotes around `-Dgroups=...` / `-Dtest=...` when there is a comma.

| Output | Path |
|--------|------|
| Surefire XML/TXT | `target/surefire-reports/` |
| Surefire HTML | `target/reports/surefire.html` |
| Sample report in repo | `docs/reports/surefire-report.html` |
| UI failure screenshots | `target/screenshots/` |
| Allure raw results | `target/allure-results/` |

## Package layout

```
src/test/java/com/homeassignment/
├── config/
├── restfulbooker/          # @Tag("api")
│   └── model/
├── graphql/                # @Tag("api")
└── ui/                     # @Tag("ui")
    ├── base/
    ├── config/
    ├── data/
    ├── pages/
    └── tests/
```

## Test Strategy

I started with API tests because they are faster and less flaky, then moved to UI.

For Restful Booker I covered auth plus a real CRUD path (create → get → update → delete) with a shared booking id. GraphQL covers both happy paths (limit, by id, variables, fragment/nested fields) and negative cases (missing id, bad syntax, unknown field).

On UI I used Page Object Model for DemoQA Practice Form and Web Tables. Assertions are AssertJ. Playwright waits are used instead of hard sleeps. Failed UI tests take a screenshot automatically.

Public sandboxes are unstable sometimes, so Restful Booker calls retry briefly on HTTP 418, and DemoQA ad banners are removed before clicks.

## Challenges & Solutions

- Restful Booker returned 418 without a proper Accept header / on burst traffic → set Accept explicitly and retry a few times.
- Hygraph returns HTTP 400 for parse/validation errors (not always 200 + errors) → assertions follow the real response shape.
- DemoQA ads blocked the Submit button → strip overlays and fall back to JS click.
- Current DemoQA web table does not sort on header click → page object enables header-click sorting so the sorting scenario can still be checked.
- Playwright `hasText("Male")` also matched "Female" → use exact text match.

## What I Would Add With More Time

- Parameterized / data-driven API cases
- Parallel UI runs with isolated contexts
- Mock fallback when a public API is down
- Publish Allure report from CI
- A few negative UI validations (empty required fields, invalid email)

## CI

`.github/workflows/tests.yml` runs API and UI jobs on push/PR, installs Chromium for UI, and uploads Surefire reports + screenshots as artifacts.
=======
# AQA_Flaming
API (REST Assured) + UI (Playwright) automation 
>>>>>>> e839cf5541b60b7ae3bc21182423e89c2bdf9f69
