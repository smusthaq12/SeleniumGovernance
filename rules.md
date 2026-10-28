# --------------------------------Selenium Codebase Governance Engine. --------------------------------

# rules.md — Duplicate Detection Rules for Java + Selenium + TestNG Automation Project

> **Purpose:** Single source of truth for the duplicate-detection agent. Every rule maps to a detector class. Any change requires SDET Architect review.
>
> **Scope:** All `.java` files under `src/test/java` and `src/main/java`.
>
> **Rule ID format:** `D<number>` (duplicates), `X<number>` (exclusions). Roadmap IDs are reserved and must not be implemented yet.

---

## 1. Scope Definition

### 1.1 Paths scanned
| Path | Include |
|------|---------|
| `src/test/java` | yes |
| `src/main/java` | yes |
| `src/test/resources/fixtures` | no (agent's own fixtures) |
| `target/`, `build/` | no |
| `generated-sources/` | no |

### 1.2 File exclusions (always ignored)
- `module-info.java`
- `package-info.java`
- Files annotated `@Generated`
- Classes annotated `@SuppressWarnings("duplicate-agent")` (see Section 4)

### 1.3 Definitions
- **Normalization**: strip comments, collapse whitespace, remove trailing semicolons inside bodies.
- **Class body hash**: SHA-256 of normalized full class body.
- **Method body hash**: SHA-256 of normalized method body (excluding signature).
- **Signature**: `<methodName>(<paramType1>,<paramType2>...)` — return type excluded.
- **Near-duplicate**: Jaccard similarity of token sets >= configured threshold (default 0.85).
- **Trivial method**: body is one of `return;`, `return <literal>;`, `return this.<field>;`, `super(<args>);`, or empty.

---

## 2. Duplicate Rules

### D1 — Duplicate Class Name (different packages)
- **Detects:** Two or more classes with the same simple name in different packages.
- **Example:** `com.team.pages.LoginPage` and `com.team.pages.legacy.LoginPage`
- **Why it matters:** Confuses imports, wrong Page Object usage in TestNG tests, maintenance cost.
- **Severity:** HIGH
- **False-positive guard:** Ignore if one is annotated `@Deprecated`.
- **Action:** Manual review — consolidate or rename.

### D2 — Duplicate Fully Qualified Name (FQN)
- **Detects:** Same FQN appears twice (identical class in two source roots).
- **Example:** `src/main/java/com/team/utils/WaitHelper.java` and `src/test/java/com/team/utils/WaitHelper.java`.
- **Severity:** HIGH (build hazard)
- **False-positive guard:** None — always a real issue.
- **Action:** Delete one immediately.

### D3 — Duplicate Method Signature in Same Class
- **Detects:** Two methods with identical name + parameter types inside one class.
- **Example:** `public void login(String u, String p)` and `public void login(String user, String pass)`.
- **Severity:** HIGH (won't compile — anomaly; merge artifact or generated code)
- **Action:** Auto-fix candidate — remove one.

### D4 — Duplicate Method Body Across Classes (exact)
#- **Detects:** Same normalized method body hash appearing in 2+ classes.
#- **Exclusions:**
#    - Trivial methods (Section 1.3)
#    - Test methods annotated `@Test` with distinct names (see X2)
#    - Constructors with trivial assignments
#    - Interface default methods intentionally shared
#- **Example:** `waitForVisible(WebElement)` implemented identically in `WaitHelper` and `LoginPage`.
#- **Severity:** MEDIUM
#- **Action:** Extract to shared `WaitHelper` / `BasePage`.

### D4 — Duplicate Method Body Across Classes (exact)
- **Detects:** Same normalized method body hash appearing in 2+ classes.
- **Exclusions:**
  - Trivial methods (Section 1.3)
  - **Standard Accessors/Getters:** Any method that only returns a field, a `null`, or a boolean (e.g., `return this.webElement;`, `return this.locator;`, `return null;`).
  - Test methods annotated `@Test` with distinct names (see X2)
  - Constructors with trivial assignments
  - Interface default methods intentionally shared
- **Example:** `waitForVisible(WebElement)` implemented identically in `WaitHelper` and `LoginPage`.
- **Severity:** MEDIUM
- **Action:** Extract to shared `WaitHelper` / `BasePage`.


### D4b — Near-Duplicate Method Body Across Classes (Structural)
- **Detects:** Methods in different classes that are highly similar (e.g., 80%+ identical) but not exact matches. This catches the "Copy-Paste-Modify" anti-pattern.
- **Example:**
    - `Method A`: `driver.findElement(By.id("btn")).click(); Assert.assertTrue(isSuccess());`
    - `Method B`: `driver.findElement(By.id("btn")).click(); Assert.assertTrue(isSuccess()); scrollDown();`
- **Algorithm:** Jaccard similarity of normalized tokens/lines >= configured threshold (default 0.80), excluding exact matches (which are caught by D4).
- **Exclusions:**
    - Trivial methods (Section 1.3)
    - Methods shorter than 5 lines (to avoid false positives on simple click actions)
    - Test methods annotated `@Test`
- **Severity:** LOW (Informational — feeds refactor backlog)
- **Action:** Review and refactor. Extract the shared logic to a `BasePage` or utility class. Use the "Template Method" pattern or composition.

### D5 — Duplicate Class Body (exact)
- **Detects:** Two classes with identical normalized class body hash (ignoring package and imports).
- **Example:** Copy-pasted `HomePage` from `pages.v1` into `pages.v2`.
- **Severity:** HIGH
- **False-positive guard:** Ignore if both classes are empty (marker classes).
- **Action:** Consolidate.

### D6 — Near-Duplicate Class Body (structural similarity)
- **Detects:** Two classes with Jaccard similarity >= threshold (default 0.85).
- **Example:** Two Page Objects with 90% same methods but one extra `logout()`.
- **Severity:** LOW (informational — refactor backlog)
- **False-positive guard:** Ignore base/child pairs (`extends` relationship — see X3).
- **Action:** Review — consider inheritance or composition.

### D7 — Duplicate Locators (Selenium-specific)
- **Detects:** Same locator string used in multiple classes.
- **Patterns matched:**
    - `By.xpath("...")`, `By.cssSelector("...")`, `By.id("...")`
    - `@FindBy(xpath = "...")`, `@FindBy(css = "...")`
    - `String LOCATOR = "..."`
- **Grouping key:** `(locatorType, locatorValue)`.
- **Exclusions:** Locators inside `Locators.java` / `constants/` package (source of truth).
- **Example:** `By.xpath("//button[@id='login']")` in both `LoginPage` and `LoginSteps`.
- **Severity:** MEDIUM
- **Action:** Move to `Locators.java` and reference via constant.

### D8 — Duplicate TestNG Test Method Body
- **Detects:** Two `@Test` methods with identical normalized body AND identical `@Test` attributes (`groups`, `priority`).
- **Why separate:** Copy-paste common but sometimes intentional (different data). Distinguish from data-driven tests.
- **Severity:** MEDIUM
- **False-positive guard:** Ignore if `@Test(dataProvider="...")` differs (see X2).
- **Action:** Merge into a `@DataProvider`-driven single test.

### D9 — Duplicate `@Test` Method Name
- **Detects:** Same `@Test` method name across multiple test classes without distinct groups.
- **Why:** TestNG reports become ambiguous; parallel execution reports collide.
- **Severity:** LOW
- **Example:** `testValidLogin()` in `LoginTest.java` and `SignupTest.java`.
- **Action:** Rename or prefix with feature domain (e.g., `login_testValidLogin`).

### D10 — Duplicate Page Object Method
- **Detects:** Method with same normalized body in two classes both extending `BasePage` or in `pages` package.
- **Why:** Missing extraction into `BasePage`.
- **Severity:** MEDIUM
- **Exclusions:** Trivial getters/setters; bodies calling only `super.<sameMethod>()`.
- **Action:** Lift into `BasePage`.

### D11 — Duplicate TestNG Listener / Factory Code
- **Detects:** Identical method bodies in classes implementing `ITestListener`, `IRetryAnalyzer`, `IAnnotationTransformer`, or containing `@DataProvider` methods.
- **Why:** Divergent retry logic is a classic Selenium anti-pattern.
- **Severity:** MEDIUM
- **Action:** Single source of truth listener under `listeners/`.

### D12 — Duplicate Constants / Env Values
- **Detects:** Same string or numeric literal repeated in 3+ classes.
- **Examples:** `"10"` (timeouts), `"https://staging.example.com"`, `"admin@example.com"`.
- **Exclusions:** Test data literal in a `@DataProvider` (allowed — is the data source).
- **Severity:** LOW
- **Action:** Extract to `ConfigReader` or `TestData`.

---

## 3. Severity Legend

| Level | CI Behavior |
|-------|-------------|
| HIGH | Build breaker after grace period (fails `mvn verify -Pdup-scan`) |
| MEDIUM | Warning — tracked in dashboard (logged, not failing) |
| LOW | Informational (logged only) |

**Grace period:** First 4 sprints — all severities warn only. After that, HIGH fails the build.

---

## 4. Suppression (False-Positive Handling)

### 4.1 Class-level suppression
```java
@SuppressWarnings("duplicate-agent")
public class LegacyLoginFlow { ... }
```

### 4.2 Targeted suppression
```java
@SuppressWarnings("duplicate-agent:D7")
```

### 4.3 Baseline file
Committed `duplicates-baseline.json` at repo root records pre-existing findings by `(ruleId, file, hash)` so only newly introduced duplicates fail CI.

- **Update requires:** PR reviewed by SDET Architect.
- **Baseline entries expire after 90 days** — forces periodic cleanup.

---

## 5. Exclusions (X-Rules) — Never Flag

| ID | Exclusion |
|----|-----------|
| X1 | Trivial methods (getters, setters, `toString`, `equals`, `hashCode`, constructor with only assignments) |
| X2 | Two `@Test` methods with identical bodies but different `@Test(dataProvider=...)` |
| X3 | Two classes with parent-child (`extends`) relationship (D6 only) |
| X4 | Locators defined inside `com.team.locators.*` or `com.team.constants.*` packages |
| X5 | Any class in `generated-sources/` or annotated `@Generated` |
| X6 | Classes implementing different interfaces with identical bodies (contract-driven similarity) |
| X7 | `@Deprecated` classes matching a non-deprecated class (D1 only) |
| X8 | Test setup/teardown (`@BeforeMethod`, `@AfterMethod`, `@BeforeClass`, `@AfterClass`) with identical `super.` delegation |
| X9 | `Assert` wrapper methods that merely delegate to TestNG's `Assert.*` with no extra logic |
| X10 | Classes with suffix `Page` vs. corresponding `Steps` class (intentional POM pattern) |

---

## 6. Configuration Defaults

| Parameter | Default | Notes |
|-----------|---------|-------|
| `similarity.threshold` | 0.85 | D6 near-duplicate |
| `min.occurrences` | 2 | Minimum count to flag |
| `min.lines.flag` | 3 | Ignore methods shorter than 3 lines (D4) |
| `severity.fail` | `HIGH` | Which severities break build after grace |
| `baseline.enabled` | true | Use `duplicates-baseline.json` |
| `report.formats` | `json,html` | |

Config file: `duplicate-agent.yml` at repo root.

---

## 7. Reporting Requirements

Each finding must include:

1. Rule ID + name (e.g., `D4 — Duplicate Method Body`)
2. Severity
3. All occurrences: `(file, class, method, line, package)`
4. Suggested action (from rule's Action field)
5. Code snippet (first 3 lines of duplicated body)
6. Summary block: total classes scanned, total findings by severity, top 5 duplicate clusters

HTML report must be sortable by rule ID and severity, grouped by package.

---

## 8. Test Fixtures Required (one per rule)

For every rule, a pair of `.java` fixtures under `src/test/resources/fixtures/D<id>/`:

- `positive/` — must produce a finding
- `negative/` — must NOT produce a finding (guards against false positives)

Example for D4:
```
fixtures/D4/
  positive/A.java
  positive/B.java
  negative/A.java
  negative/B.java
```

Any new rule must ship with positive + negative fixtures before merge.

---

## 9. Governance

| Change Type | Approver | Requires |
|-------------|----------|----------|
| Add new rule | SDET Architect | `rules.md` update + positive/negative fixtures |
| Change severity | SDET Architect | Sprint planning note |
| Change default threshold | SDET Architect | Team vote + baseline re-gen |
| Add suppression to a class | Any dev (peer review) | Comment in code stating reason |
| Update `duplicates-baseline.json` | SDET Architect | PR description listing removed entries |

---

## 10. Roadmap (post-MVP rules — do not implement yet)

- D13 — Duplicate `driver.findElement` chains inside test methods (should be Page Object)
- D14 — Duplicate `Thread.sleep(...)` calls (anti-pattern detector)
- D15 — Duplicate TestNG `@DataProvider` names across suites
- D16 — Duplicate `properties` keys across env files
- D17 — Duplicate `ExtentTest` / `Allure` step descriptions

---

**End of `rules.md`.**
Every detector class's Javadoc must reference its rule ID from this document. Any rule not listed here cannot be implemented.