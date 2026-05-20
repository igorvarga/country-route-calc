# Country Route Calculator

Spring Boot service that calculates a land route between two countries by walking the border graph from [mledoze/countries](https://github.com/mledoze/countries). By default it returns a deterministic fewest-border-crossings route using BFS.

## Prerequisites

JDK 21 and Maven 3.9+ on `PATH`.

## Build & run

```
mvn clean package
java -jar target/country-route-calc-*.jar
# or:
mvn spring-boot:run
```

Listens on `http://localhost:8080`.

## Usage

```
$ curl http://localhost:8080/v1/routes/CZE/ITA
{"origin":"CZE","destination":"ITA","steps":2,"route":["CZE","AUT","ITA"]}
```

Country codes are [ISO 3166-1 alpha-3](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3) (`cca3`), case-insensitive on input.

Errors return an [RFC 7807](https://datatracker.ietf.org/doc/html/rfc7807) ProblemDetail body:

- `400 Bad Request` — malformed code (not three letters)
- `404 Not Found` — unknown country code, or no land route (e.g. island to mainland)

## Common overrides

```
# Offline mode (bundled snapshot, no network)
java -jar target/country-route-calc-*.jar --countries.source=embedded

# Local file
java -jar target/country-route-calc-*.jar --countries.source=local --countries.local.path=/tmp/countries.json

# Env vars (Spring relaxed-binding)
COUNTRIES_SOURCE=embedded java -jar target/country-route-calc-*.jar

# Different port
java -jar target/country-route-calc-*.jar --server.port=9090
```

## Tests

```
mvn verify
```

Runs tests plus Spotless (google-java-format) and PMD checks. Apply formatting with:

```
mvn spotless:apply
```

## More

- [PLAN.md](PLAN.md) — design and architectural decisions
- [TODO.md](TODO.md) — deferred items
- [CLAUDE.md](CLAUDE.md) — project conventions
