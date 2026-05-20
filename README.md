# Country Route Calculator

Spring Boot service that calculates a land route between two countries by walking the border graph from [mledoze/countries](https://github.com/mledoze/countries).

## Prerequisites

JDK 25 and Maven 3.9+ on `PATH`. The build is JDK 25–specific.

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
$ curl http://localhost:8080/routing/CZE/ITA
{"route":["CZE","AUT","ITA"]}
```

Country codes are [ISO 3166-1 alpha-3](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-3) (`cca3`), case-insensitive on input.

Errors return HTTP 400 with an [RFC 7807](https://datatracker.ietf.org/doc/html/rfc7807) ProblemDetail body for: unknown country code, malformed code (not three letters), or no land route (e.g. island to mainland).

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
mvn test
```

## More

- [PLAN.md](PLAN.md) — design and architectural decisions
- [TODO.md](TODO.md) — deferred items
- [CLAUDE.md](CLAUDE.md) — project conventions
