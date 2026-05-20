# CLAUDE.md

Project conventions for Claude Code (and any teammate working on this repo).

## Commit messages

Use [Conventional Commits](https://www.conventionalcommits.org/): `<type>[optional scope]: <description>`.

Types in use:
- `feat` — new feature
- `fix` — bug fix
- `docs` — documentation only
- `refactor` — code change that neither fixes a bug nor adds a feature
- `test` — adding or correcting tests
- `chore` — build process, dependency bumps, tooling
- `perf` — performance improvement
- `style` — formatting only
- `build` / `ci` — build system or CI changes

Add a scope when it clarifies the affected area, e.g. `feat(routing): add A* finder`, `fix(loader): handle asymmetric borders`. Mark breaking changes with `!` after the type/scope or a `BREAKING CHANGE:` footer.

If a change spans multiple types, split into separate commits.

Examples:
- `feat: add Dijkstra route finder`
- `fix(loader): skip border references to unknown cca3`
- `docs: update PLAN.md with config best practices`
- `chore: bump Spring Boot to 3.5.1`
- `test(routing): cover origin == destination case`
