# Implementation Plan: Country Route Calculation

## Stack
- **JDK 25**, **Spring Boot 3.5.x**
- **Prerequisites**: JDK 25 and Maven 3.9+ must be on `PATH`. The build is JDK 25–specific; older JDKs will not compile.
- Java package: `hr.oblivion.countryroute`
- Maven artifact: `country-route-calc`

## 1. Pluggable Algorithm (Strategy pattern)
- **Interface**: `RouteFinder { Optional<List<String>> find(String origin, String destination); }`
- **Implementations**:
  - `BfsRouteFinder` — deterministic fewest-border-crossings route, O(V+E); best match for the spec's "any possible land route"
- **Selection**: `routing.algorithm=bfs` in `application.properties`. `RoutingConfig` resolves the active implementation from Spring's `Map<String, RouteFinder>` so adding a future algorithm only requires another named `RouteFinder` bean and a config value.
- **Validation**: unknown values throw `IllegalStateException` during startup with the available finder keys.
- **Determinism**: BFS sorts neighbors before traversal so equal-hop alternatives resolve predictably.

## 2. Pluggable JSON Source (Strategy pattern)
- **Interface**: `CountrySource { CountryDataset load(); }`
- **Implementations**:
  - `RemoteCountrySource` *(default)* — fetches from GitHub raw URL using Spring's `RestClient`
  - `EmbeddedCountrySource` — reads bundled `src/main/resources/data/countries.json` (used for tests + offline mode); JSON is **manually committed** to the repo — refresh with `curl -o src/main/resources/data/countries.json https://raw.githubusercontent.com/mledoze/countries/master/countries.json`. No build-time network dependency.
  - `LocalFileCountrySource` — reads configurable path on disk
- **Failure mode**: **fail-fast at startup** if the configured source can't load (network error, bad path, malformed JSON). Application logs the cause and exits non-zero. Operator can switch to `countries.source=embedded` to keep running.
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

**Endpoint**: `GET /routing/{origin}/{destination}` → `{ "route": [...] }`

**Input handling**:
- cca3 path variables normalized to uppercase before lookup (accepts `cze`, `Cze`, `CZE` identically — defensive, avoids surprises)
- After normalization, validated against `[A-Z]{3}` — malformed input → 400

**Response cases**:

| Case | Status | Body |
|---|---|---|
| Route found | 200 | `{ "route": ["CZE", "AUT", "ITA"] }` |
| Origin == destination | 200 | `{ "route": ["CZE"] }` — degenerate route, zero crossings, journey trivially exists |
| Unknown cca3 | 400 | ProblemDetail, `type=urn:country-route-calc:errors:unknown-country-code`, extensions: `code`, `field` (`origin`/`destination`) |
| No land route (e.g. `ISL → DEU`) | 400 | ProblemDetail, `type=urn:country-route-calc:errors:no-land-route`, extensions: `origin`, `destination` |
| Malformed cca3 | 400 | ProblemDetail, `type=urn:country-route-calc:errors:invalid-country-code`, extensions: `value`, `field` |

**Error format**: RFC 7807 Problem Details (`application/problem+json`), produced via Spring 6's built-in `ProblemDetail` + `@RestControllerAdvice`. Standard fields (`type`, `title`, `status`, `detail`, `instance`) plus typed extensions per case. Spring Boot 3 idiomatic — no bespoke error envelope.

**Implementation split**:
- Controller normalizes + format-validates path variables, then checks existence against the loaded country set; missing → `UnknownCountryException`
- `RouteFinder` is called only with valid, known codes; `Optional.empty()` means "no land route" → controller raises `NoRouteException`
- `@RestControllerAdvice` translates both exceptions (plus format-validation failures) to ProblemDetail responses

## 4. Tests
- `BfsRouteFinderTest` — verifies fixture graph traversal for linear, direct-border, disconnected, same-node, and unknown-node cases
- `EmbeddedCountrySourceTest` — Jackson mapping from a trimmed JSON fixture
- `RemoteCountrySourceTest` — `MockRestServiceServer` verifies the URL is hit correctly and JSON parses end-to-end
- `RoutingControllerIT` — `@SpringBootTest` with `countries.source=embedded` so tests stay offline + deterministic. Covers:
  - Spec example `CZE → ITA` → 200 (default BFS yields `["CZE","AUT","ITA"]` for this pair)
  - Origin == destination `CZE → CZE` → 200 with `["CZE"]`
  - Island `ISL → DEU` → 400 (no-land-route ProblemDetail)
  - Unknown code `ZZZ → ITA` → 400 (unknown-country-code ProblemDetail)
  - Lowercase normalization `cze → ita` → 200
  - Malformed code `CZ → ITA` → 400 (invalid-country-code ProblemDetail)

## 5. Build & Run
- `mvn clean package` → produces runnable jar at `target/country-route-calc-<version>.jar`
- `mvn spring-boot:run` (foreground) or `java -jar target/country-route-calc-*.jar`
- Default port **8080** (override with `--server.port=9090`)
- Sample request:
  ```
  curl http://localhost:8080/routing/CZE/ITA
  # → {"route":["CZE","AUT","ITA"]}
  ```
- **Overrides** (pick whichever fits the workflow — see §6 for precedence):
  ```
  # CLI args (one-off runs, CI)
  java -jar target/country-route-calc-*.jar --countries.source=local --countries.local.path=/tmp/countries.json

  # Env vars (containers, 12-factor; Spring relaxed-binding)
  COUNTRIES_SOURCE=local COUNTRIES_LOCAL_PATH=/tmp/countries.json java -jar target/country-route-calc-*.jar

  # Profile (tagged set of overrides, e.g. personal dev settings)
  java -jar target/country-route-calc-*.jar --spring.profiles.active=local
  ```
- **README.md** to be generated as a brief, minimal version of this section + §6 — covering prereqs, build, run, sample request, common overrides. Satisfies SPEC's "build & run instructions" deliverable.

## 6. Configuration

**Goal**: clone → `mvn spring-boot:run` works without local edits; operators can override anything without rebuilding.

### Files committed to the repo
| File | Purpose |
|---|---|
| `src/main/resources/application.properties` | Defaults that boot the app correctly out of the box; doubles as documentation of every knob |
| `src/test/resources/application.properties` | Test-classpath overrides (forces `countries.source=embedded`); test classpath wins over main during `mvn test` — no `@ActiveProfiles` needed |
| `application-local.properties.example` | Template showing available fields, with placeholder values only |

### Files **not** committed (gitignored)
| File | Purpose |
|---|---|
| `application-local.properties` | Per-developer overrides; activated via `--spring.profiles.active=local` |

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
| Algorithm alternatives | None in mainline | Dijkstra/geographic weighting was removed from mainline as unnecessary for the spec; preserved in git branch `dijkstra-distance-routing` |
| Default source | Remote | Spec says fetch from that URL |
| Test source | Embedded | Reproducible, offline, fast |
| Error format | RFC 7807 ProblemDetail | Spring Boot 3 idiomatic, no bespoke envelope |
| Source failure mode | Fail-fast at startup | Operator awareness > silent degradation; switch to embedded via config |
| cca3 input | Normalize to uppercase, validate `[A-Z]{3}` | Defensive against client casing; lets us distinguish malformed vs unknown vs no-route |
| Config layout | Grouped per source, typed `@ConfigurationProperties` + `@Validated` | Idiomatic Spring; fails fast on misconfig; scales when sources gain fields (timeout, auth, etc.) |
| Config override path | CLI > env > profile > committed defaults | Standard Spring precedence; lets operators tweak without rebuilding |
