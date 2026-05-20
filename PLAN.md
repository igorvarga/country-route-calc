# Implementation Plan: Country Route Calculation

## Stack
- **JDK 21**, **Spring Boot 3.5.x**
- **Prerequisites**: JDK 21 and Maven 3.9+ must be on `PATH`.
- Java package: `hr.oblivion.countryroute`
- Maven artifact: `country-route-calc`

## 1. Route Algorithm
- **Interface**: `RouteFinder { Optional<List<String>> find(String origin, String destination); }`
- **Implementations**:
  - `BfsRouteFinder` — deterministic fewest-border-crossings route, O(V+E); best match for the spec's "any possible land route"
- **Selection**: `routing.algorithm=bfs` in `application.properties`. `RoutingConfig` resolves the active implementation from Spring's `Map<String, RouteFinder>` so adding a future algorithm only requires another named `RouteFinder` bean and a config value.
- **Validation**: unknown values throw `IllegalStateException` during startup with the available finder keys.
- **Determinism**: `CountryGraph` pre-sorts adjacency lists once when built, so equal-hop alternatives resolve predictably without per-request sorting.

## 2. Pluggable JSON Source (Strategy pattern)
- **Interface**: `CountrySource { CountryDataset load(); }`
- **Implementations**:
  - `RemoteCountrySource` *(default)* — fetches from GitHub raw URL using Spring's `RestClient`
  - `EmbeddedCountrySource` — reads bundled `src/main/resources/data/countries.json` (used for tests + offline mode); JSON is **manually committed** to the repo — refresh with `curl -o src/main/resources/data/countries.json https://raw.githubusercontent.com/mledoze/countries/master/countries.json`. No build-time network dependency.
  - `LocalFileCountrySource` — reads configurable path on disk
- **Failure mode**: **fail-fast at startup** if the configured source can't load (network error, bad path, malformed JSON). Application logs the cause and exits non-zero. Operator can switch to `countries.source=embedded` to keep running.
- **Metadata**: startup logs source, concrete location, loaded timestamp, and country count.
- **Loader behavior** (applies after parsing, regardless of source):
  - Graph treated as **undirected**: edge (A, B) exists if A lists B as border OR B lists A
  - Asymmetric border entries logged at WARN — diagnostic only, not blocking
  - Border references to unknown cca3 logged at WARN and skipped
- **Config** (grouped per source — only the active source's keys are consulted):
  ```
  countries.source=remote   # remote | embedded | local

  # used only when source=remote:
  countries.remote.url=https://raw.githubusercontent.com/mledoze/countries/master/countries.json

  # used only when source=local:
  countries.local.path=/path/to/countries.json

  # source=embedded uses src/main/resources/data/countries.json — no config keys
  ```
- Bound to a typed `@ConfigurationProperties("countries")` record with `@Validated` (see §6) so misconfig fails fast at startup with a clear message.

## 3. REST API

**Endpoint**: `GET /v1/routes/{origin}/{destination}` → `{ "origin": "...", "destination": "...", "steps": N, "route": [...] }`

URL is versioned under `/v1` and uses the plural resource noun `routes` (REST convention). Deviates from SPEC.md's `/routing/...` — intentional, see Key Decisions below.

**Input handling**:
- cca3 path variables format-validated via `@Pattern("[A-Za-z]{3}")` on `@Validated` controller — malformed input → 400 with `ConstraintViolationException` translated to ProblemDetail
- Inside the handler, normalized to uppercase before graph lookup (accepts `cze`, `Cze`, `CZE` identically)

**Response cases**:

| Case | Status | Body |
|---|---|---|
| Route found | 200 | `{ "origin":"CZE", "destination":"ITA", "steps":2, "route":["CZE","AUT","ITA"] }` |
| Origin == destination | 200 | `{ "origin":"CZE", "destination":"CZE", "steps":0, "route":["CZE"] }` — degenerate route, journey trivially exists |
| Unknown cca3 | 404 | ProblemDetail, `type=urn:country-route-calc:errors:unknown-country-code`, extensions: `value`, `field` (`origin`/`destination`) |
| No land route (e.g. `ISL → DEU`) | 404 | ProblemDetail, `type=urn:country-route-calc:errors:no-land-route`, extensions: `origin`, `destination` |
| Malformed cca3 | 400 | ProblemDetail, `type=urn:country-route-calc:errors:invalid-country-code`, extensions: `value`, `field` |

400 is reserved for "you sent something syntactically bad"; 404 covers "what you asked for doesn't exist in the dataset / can't be computed". Deviates from SPEC.md (which specifies 400 across the board) — intentional.

**Error format**: RFC 7807 Problem Details (`application/problem+json`), produced via Spring 6's built-in `ProblemDetail` + `@RestControllerAdvice`. Standard fields (`type`, `title`, `status`, `detail`, `instance`) plus typed extensions per case. Spring Boot 3 idiomatic — no bespoke error envelope.

**Implementation split**:
- `@Validated` + `@Pattern` reject malformed cca3 before the handler body runs; the resulting `ConstraintViolationException` is translated to the invalid-country-code ProblemDetail in `GlobalExceptionHandler`
- Controller uppercases the (now validated) inputs and checks existence against the loaded country set; missing → `UnknownCountryException`
- `RouteFinder` is called only with valid, known codes; `Optional.empty()` means "no land route" → controller raises `NoRouteException`
- `@RestControllerAdvice` translates all three exceptions to ProblemDetail responses

## 4. Tests
- `BfsRouteFinderTest` — verifies fixture graph traversal for linear, direct-border, disconnected, same-node, and unknown-node cases
- `EmbeddedCountrySourceTest` — Jackson mapping from a trimmed JSON fixture
- `CountryGraphTest` — verifies undirected graph construction, sorted neighbors, islands, and skipped unknown borders
- `CountryRouteApplicationTest` — verifies the Spring context loads with the embedded dataset and graph
- `RoutingControllerTest` — `@SpringBootTest` with `countries.source=embedded` so tests stay offline + deterministic. Covers:
  - Spec example `CZE → ITA` → 200 (default BFS yields `["CZE","AUT","ITA"]` for this pair)
  - Active route finder is `BfsRouteFinder`
  - Origin == destination `CZE → CZE` → 200 with `["CZE"]`
  - Island `ISL → DEU` → 404 (no-land-route ProblemDetail)
  - Unknown origin `ZZZ → ITA` → 404 (unknown-country-code ProblemDetail)
  - Unknown destination `CZE → ZZZ` → 404 (unknown-country-code ProblemDetail)
  - Lowercase normalization `cze → ita` → 200
  - Malformed code `CZ → ITA` → 400 (invalid-country-code ProblemDetail)

## 5. Build & Run
- `mvn clean package` → produces runnable jar at `target/country-route-calc-<version>.jar`
- `mvn spring-boot:run` (foreground) or `java -jar target/country-route-calc-*.jar`
- Default port **8080** (override with `--server.port=9090`)
- Sample request:
  ```
  curl http://localhost:8080/v1/routes/CZE/ITA
  # → {"origin":"CZE","destination":"ITA","steps":2,"route":["CZE","AUT","ITA"]}
  ```
- **Overrides** (pick whichever fits the workflow — see §6 for precedence):
  ```
  # CLI args (one-off runs, CI)
  java -jar target/country-route-calc-*.jar --countries.source=local --countries.local.path=/tmp/countries.json

  # Env vars (containers, 12-factor; Spring relaxed-binding)
  COUNTRIES_SOURCE=local COUNTRIES_LOCAL_PATH=/tmp/countries.json java -jar target/country-route-calc-*.jar

  # Embedded offline mode
  COUNTRIES_SOURCE=embedded java -jar target/country-route-calc-*.jar
  ```
- `README.md` covers prerequisites, build, run, sample request, common overrides, and tests. This satisfies SPEC's "build & run instructions" deliverable.

## 6. Configuration

**Goal**: clone → `mvn spring-boot:run` works without local edits; operators can override anything without rebuilding.

### Files committed to the repo
| File | Purpose |
|---|---|
| `src/main/resources/application.properties` | Defaults that boot the app correctly out of the box; doubles as documentation of every knob |
| `src/test/resources/application.properties` | Test-classpath overrides (forces `countries.source=embedded`); test classpath wins over main during `mvn test` — no `@ActiveProfiles` needed |

### Override precedence (highest → lowest)
1. CLI args: `--countries.source=local`
2. OS env vars: `COUNTRIES_SOURCE=local` (relaxed-binding converts `countries.source` ↔ `COUNTRIES_SOURCE`)
3. `application-{profile}.properties` — outside jar overrides inside jar
4. `application.properties` — outside jar overrides inside jar
5. Defaults declared on `@ConfigurationProperties` classes

External config without rebuilding:
```
java -jar app.jar --spring.config.location=optional:file:./config/
```

### Typed binding + validation
All config keys bind to typed records with `@Validated`:
```java
@ConfigurationProperties("countries")
@Validated
record CountriesConfig(
    @NotNull Source source,
    RemoteCfg remote,
    LocalCfg local
) {
  // @URL = org.hibernate.validator.constraints.URL (Hibernate Validator; jakarta has no equivalent)
  record RemoteCfg(@URL String url) {}
  record LocalCfg(Path path) {}
}
```
Typos and missing required fields fail at startup with a clear message — operators don't discover misconfig three layers deep at runtime.

### Secrets
Not applicable here (public URL, no auth). General rule for any future field that could be a secret: env vars only, never committed properties files; reference via `${ENV_VAR}` placeholders if a properties file needs to mention the key.

## Key Decisions
| | Choice | Why |
|---|---|---|
| Default algorithm | BFS | The spec asks for any possible land route; BFS is efficient and returns a deterministic fewest-border-crossings route |
| Algorithm extension point | `RouteFinder` + `RoutingConfig` | Mainline stays BFS-only, but another named `RouteFinder` can be added later without changing controller code |
| Default source | Remote | Spec says fetch from that URL |
| Test source | Embedded | Reproducible, offline, fast |
| Error format | RFC 7807 ProblemDetail | Spring Boot 3 idiomatic, no bespoke envelope |
| Source failure mode | Fail-fast at startup | Operator awareness > silent degradation; switch to embedded via config |
| API URL shape | `/v1/routes/{origin}/{destination}` — versioned, plural resource noun | REST convention (plural collection + identifier); `/v1` prefix lets future breaking changes ship as `/v2` without breaking existing clients. Deviates from SPEC.md's `/routing/...` — intentional, applied during the pre-release hardening pass |
| HTTP status mapping | 400 for malformed input only; 404 for unknown country & no-route | 400 means "you sent something syntactically bad"; 404 means "what you asked for doesn't exist". Deviates from SPEC.md (which specifies 400 across the board) |
| cca3 input | `@Validated` + `@Pattern("[A-Za-z]{3}")` on path vars, uppercased before lookup | Declarative validation; `ConstraintViolationException` translates to invalid-country-code ProblemDetail uniformly with other errors |
| Config layout | Grouped per source, typed `@ConfigurationProperties` + `@Validated` | Idiomatic Spring; fails fast on misconfig; scales when sources gain fields (timeout, auth, etc.) |
| Config override path | CLI > env > profile > committed defaults | Standard Spring precedence; lets operators tweak without rebuilding |
