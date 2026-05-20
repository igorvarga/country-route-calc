# TODO

Items deferred from the initial plan to keep the surface tight against spec. Tracked here so they're not forgotten.

## Dataset version pinning + metadata
Make the loaded dataset reproducible across deployments by pinning a specific version per source, and expose what was loaded:

- `RemoteCountrySource` — make the mledoze git ref (tag/branch/commit) configurable via `countries.remote.version`; build URL via `UriComponentsBuilder` with `{version}` placeholder expanded by `buildAndExpand(version)` (Spring-idiomatic). Default to a known good tag (e.g. `v3.1`) rather than `master` so deployments don't silently drift.
- `EmbeddedCountrySource` — bundle multiple snapshots as `countries-<version>.json` side-by-side; pick via `countries.embedded.version`.
- `LocalFileCountrySource` — accept user-declared version via `countries.local.version`.
- In-memory `DatasetMetadata { version, source, loadedAt, countryCount }` record produced by the loader.
- Log on startup so the deployed version is visible.

**Why deferred**: not required by spec; the basic source pluggability covers the spec ask. Versioning becomes valuable once there are multiple deployments with potentially different snapshots — until then it's overhead.

## `GET /dataset` endpoint
Expose loaded dataset metadata (source, version, loadedAt, countryCount) via HTTP for runtime introspection. Currently only logged at startup; an endpoint would help ops diagnose which dataset version is live in a deployed instance without shell access to logs.

- Depends on `DatasetMetadata` from the versioning entry above
- Trivial once metadata is held in memory by the loader — just inject into a `@RestController` returning JSON
- Add a `RoutingControllerIT` case once implemented

## Source-bytes checksum (SHA-256)
Record SHA-256 of raw JSON bytes in `DatasetMetadata` so identical content across sources is detectable. Useful for:
- Verifying that an embedded snapshot matches what the remote serves at a given tag
- Caching layer integrity checks (if one is added later)

Skipped for now — adds a hashing step and metadata field with no current consumer.

## OpenAPI / Swagger documentation
Add `springdoc-openapi-starter-webmvc-ui` so the API is self-documenting at `/swagger-ui.html` and machine-readable at `/v3/api-docs`. Industry table stakes for a public REST API; pairs naturally with the existing `RoutingController` and `GlobalExceptionHandler` (the latter's ProblemDetail responses surface in the spec via `@ApiResponse`).

- Add the dependency
- Annotate `RoutingController` with `@Operation`, `@Parameter`, and per-status `@ApiResponse`
- Configure a stable `info` block (title, version) so `/v3/api-docs` is consumable by codegen

## Spring Boot Actuator
Add `spring-boot-starter-actuator` for the standard ops endpoints: `/actuator/health`, `/actuator/info`, `/actuator/metrics`. Required for k8s liveness/readiness probes and any Prometheus-style scraping.

- Add the dependency
- Expose only `health` and `info` by default; gate `metrics`/`env` behind security if added
- Pair `/actuator/info` with the deferred `DatasetMetadata` entry above so operators can see which dataset version is live

## Bidirectional BFS
If the graph grows substantially beyond the current country-border dataset, consider adding `BidirectionalBfsRouteFinder` as another `RouteFinder` implementation.

- Same result quality as BFS: fewest border crossings
- Potentially fewer explored nodes for long routes by searching from both origin and destination
- Not needed for the current ~250-country graph; normal BFS is simpler and fast enough
