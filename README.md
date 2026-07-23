# QA Automation Test Suite

**Vasyl Kachala** · Flamingo AQA Assignment

[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![REST Assured](https://img.shields.io/badge/REST%20Assured-5.x-5B9BD5)](https://rest-assured.io/)
[![Playwright](https://img.shields.io/badge/Playwright-Java-2EAD33?logo=playwright&logoColor=white)](https://playwright.dev/java/)
[![AssertJ](https://img.shields.io/badge/AssertJ-assertions-0078D4)](https://assertj.github.io/doc/)

API + UI automation against public practice services.

| Layer | System | Link |
|-------|--------|------|
| REST | Restful Booker | https://restful-booker.herokuapp.com |
| GraphQL | Hygraph (Video schema) | https://hygraph.com/graphql-playground |
| UI | DemoQA | https://demoqa.com |

---

## Prerequisites

- Java 17+
- Maven 3.6+
- Chrome / Chromium (Playwright installs its own browser on first setup)

---

## How to Run

```bash
# One-time: install Playwright Chromium
mvn exec:java -e -Dexec.classpathScope=test -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"

# Run all tests
mvn clean test

# Run only API tests
mvn test "-Dgroups=api"

# Run only UI tests
mvn test "-Dgroups=ui"

# Surefire HTML report (after tests)
mvn surefire-report:report-only

# Allure report (optional)
mvn allure:serve
```

> **Windows PowerShell:** quote `-Dgroups=...` when needed, e.g. `mvn test "-Dgroups=api"`.

### Reports

| Report | Path |
|--------|------|
| Surefire XML / TXT | `target/surefire-reports/` |
| Surefire HTML | `target/reports/surefire.html` |
| Sample report in repo | [`docs/reports/surefire-report.html`](docs/reports/surefire-report.html) |
| UI failure screenshots | `target/screenshots/` |
| Allure results | `target/allure-results/` |

---

## Coverage overview

| Area | What is covered | Count (approx.) |
|------|-----------------|-----------------|
| REST | Auth + booking CRUD | 6 tests |
| GraphQL | Positive + negative | 7 tests |
| UI | Practice Form + Web Tables (POM) | 6 tests |

Tags: `@Tag("api")` / `@Tag("ui")` → Surefire `-Dgroups=api|ui`.

---

## Project structure

```text
src/test/java/com/homeassignment/
├── config/                  # shared API config
├── restfulbooker/           # REST tests  (@Tag api)
│   └── model/
├── graphql/                 # GraphQL tests (@Tag api)
└── ui/                      # DemoQA UI   (@Tag ui)
    ├── base/                # browser lifecycle + screenshots on fail
    ├── config/
    ├── data/                # Lombok DTOs
    ├── pages/               # Page Object Model
    └── tests/
```

Also in the repo:

- `pom.xml` — dependencies and plugins
- `.gitignore`
- `.github/workflows/tests.yml` — CI (bonus)
- `docs/reports/surefire-report.html` — sample test report

---

## Test Strategy

I started with **API tests** (fast feedback, less flaky), then added **UI**.

**REST (Restful Booker)**  
Authentication, then a real CRUD flow: create → get by id → update → delete. Shared booking id + token. Short retry on HTTP `418` because the public Heroku app is unstable.

**GraphQL (Hygraph Video)**  
Positive: list with limit, entity by id, variables, fragment + nested fields (`movie → publishedBy → name`).  
Negative: non-existent id, malformed query, unknown field — assertions follow the **real** Hygraph response shape (including HTTP 400 for parse/validation).

**UI (DemoQA)**  
Page Object Model for Practice Form (fill, upload, date picker, dropdowns, success modal) and Web Tables (add / edit / delete / search / sorting). AssertJ for assertions. Playwright auto-waits + explicit waits — no hard sleeps in tests. Failed UI tests capture a screenshot automatically.

---

## Challenges & Solutions

| Challenge | Solution |
|-----------|----------|
| Restful Booker returns `418` | Explicit `Accept: application/json`, Jackson body, short retry |
| Hygraph errors often come as HTTP `400` | Assert observed status + `errors[]` / `data` |
| DemoQA ads block Submit | Remove overlays; JS click fallback |
| Web Tables headers no longer sort | Page object enables header-click sorting for the scenario |
| `hasText("Male")` also matched Female | Exact text match inside `#genterWrapper` |

---

## What I Would Add With More Time

- Data-driven / parameterized API cases
- Parallel UI execution with isolated browser contexts
- Mock fallback when a public service is down
- Publish Allure from CI to GitHub Pages
- Extra negative UI checks (empty required fields, invalid email)

---

## CI/CD (bonus)

Workflow: [`.github/workflows/tests.yml`](.github/workflows/tests.yml)

- Runs on push / PR to `main`
- Separate jobs for API (`-Dgroups=api`) and UI (`-Dgroups=ui`)
- Installs Chromium for UI
- Uploads Surefire reports and screenshots as artifacts

---

## Tech stack

| Purpose | Library |
|---------|---------|
| Runner | JUnit 5 |
| API | REST Assured + Jackson |
| UI | Playwright for Java + POM |
| Assertions | AssertJ |
| Reporting | Surefire HTML, Allure |
| DX | Lombok |
