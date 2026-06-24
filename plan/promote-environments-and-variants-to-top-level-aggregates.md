# Promote Environments and Variants to Top-Level Aggregates

## Context
Today the service has a **single** event-sourced aggregate, `FeatureFlag` (one event
stream per flag UUID), that nests *environments by name* and *variants by name/value*
as inline values. They are managed through name-in-path URLs like
`POST /flags/{flagId}/environments/{name}` and `DELETE /flags/{flagId}/variants/{id}`,
and flag rename lives at `PATCH /flags/{flagId}/name`.

The user wants environments and variants to be **independent, reusable, top-level
resources** that flags merely *reference by UUID*. Associations are edited
declaratively on the flag itself, name-in-path sub-resources are removed, references
are validated, and deleting a shared resource is preceded by a usage check and
cascades a cleanup of the references. This makes environments/variants first-class and
shareable across flags instead of being copies trapped inside one flag.

## Decisions (confirmed with user)
- Three aggregates: **FeatureFlag**, **Environment** (id + name + description),
  **Variant** (id + value). Each gets its own event stream / CRUD endpoints.
- Flags hold only `Set<UUID> environmentIds` and `Set<UUID> variantIds`.
- Flag edits are declarative: `PATCH /flags/{flagId}` with any of
  `{name?, environmentIds?, variantIds?}`; each present field **replaces** that value/set.
  `POST /flags` also accepts optional `environmentIds`/`variantIds`.
- `PATCH /flags/{flagId}/name` and all `/flags/{flagId}/environments...` &
  `/flags/{flagId}/variants...` sub-resource routes are **deleted**.
- Attaching a UUID **validates** the target exists and is not deleted → `400` otherwise.
- Deletion is two-step: `GET /environments/{id}/usage` (and `/variants/{id}/usage`)
  reports referencing flags; `DELETE` then **proceeds regardless**, stripping the UUID
  from every referencing flag and appending the delete event.
- A maintained **reverse-index projection** (`envId/variantId → flagIds`) backs usage
  lookups and cascade cleanup.

## Event model
`DomainEvent` becomes a base sealed interface (`UUID aggregateId()`, `Instant occurredAt()`)
that `permits` three sub-interfaces. Each sub-interface declares its domain-named id and
a `default aggregateId()` delegating to it, so the generic `EventStore` keeps working and
each aggregate switches exhaustively over only its own events:

- `FlagEvent` (`UUID flagId(); default aggregateId(){return flagId();}`) permits
  `FeatureFlagCreatedEvent` (now carries initial `Set<UUID> environmentIds`,
  `Set<UUID> variantIds`), `FeatureFlagDeletedEvent`, and **new**
  `FlagUpdatedEvent(flagId, String name|null, Set<UUID> environmentIds|null,
  Set<UUID> variantIds|null, occurredAt)` — `null` fields = unchanged. This single event
  covers every flag mutation (rename, environment-set replace, variant-set replace),
  replacing `FeatureFlagRenamedEvent` and the separate per-field change events.
- `EnvironmentEvent` (`UUID environmentId()`) permits **new** `EnvironmentCreatedEvent`,
  `EnvironmentUpdatedEvent`, `EnvironmentDeletedEvent`.
- `VariantEvent` (`UUID variantId()`) permits **new** `VariantCreatedEvent`,
  `VariantUpdatedEvent`, `VariantDeletedEvent`.

**Delete these events**: `FeatureFlagRenamedEvent` (folded into `FlagUpdatedEvent`) and the
old in-flag ones — `EnvironmentAddedEvent`, `EnvironmentRemovedEvent`, `VariantAddedEvent`,
`VariantRemovedEvent`, `VariantModifiedEvent`.

## Files to change

### Domain — `src/main/java/com/featureflags/domain/`
- `FeatureFlag.java` — replace `Map<String,Environment>` / `Map<UUID,Variant>` with
  `Set<UUID> environmentIds`, `Set<UUID> variantIds`; `apply(FlagEvent)`; add
  `environmentIds()` / `variantIds()` accessors; keep `reconstitute`, `isDeleted`.
- `Environment.java` — **repurpose** the current `record Environment(String name)` into an
  event-sourced aggregate (id, name, description, deleted) mirroring `FeatureFlag`.
- `Variant.java` — **repurpose** the current `record Variant(...)` into an event-sourced
  aggregate (id, value, deleted).

### Commands — `src/main/java/com/featureflags/command/`
- `CreateFeatureFlagCommand(name, Set<UUID> environmentIds, Set<UUID> variantIds)`.
- **New** `UpdateFeatureFlagCommand(flagId, String name|null, Set<UUID> environmentIds|null,
  Set<UUID> variantIds|null)` — `null` = leave unchanged (folds in rename).
- Keep `DeleteFeatureFlagCommand`. **Delete** `RenameFeatureFlagCommand`,
  `AddEnvironmentCommand`, `RemoveEnvironmentCommand`, `AddVariantCommand`,
  `RemoveVariantCommand`, `ModifyVariantCommand`.
- **New** `CreateEnvironmentCommand`, `UpdateEnvironmentCommand`, `DeleteEnvironmentCommand`
  and `CreateVariantCommand`, `UpdateVariantCommand`, `DeleteVariantCommand`
  (reuse `CommandValidation`).

### Store / projection — `src/main/java/com/featureflags/store/` (+ new `projection/`)
- `EventStore` / `InMemoryEventStore` — add listener registration
  (`addListener(Consumer<List<DomainEvent>>)`); invoke listeners **after** a successful
  append (outside the `compute` lambda) so projections stay synchronously consistent.
- **New** `projection/FlagReferenceIndex.java` (`@Component`, registers as a store
  listener): maintains `environmentId→Set<flagId>`, `variantId→Set<flagId>`, and
  `flagId→(envIds,varIds)` for diffing; updates on `FlagEvent`s (incl. removing the flag
  on `FeatureFlagDeletedEvent`). Exposes `flagsUsingEnvironment(UUID)` /
  `flagsUsingVariant(UUID)`.

### Handlers — `src/main/java/com/featureflags/handler/`
- `FeatureFlagCommandHandler` — `create` (validate each env/variant id exists & not
  deleted by loading its stream → reconstitute; else `InvalidReferenceException`),
  `update` (same validation for replaced sets; emit a single `FlagUpdatedEvent` carrying
  only the changed fields), `delete`.
- **New** `EnvironmentCommandHandler` — create/update/delete + `usage(envId)`. `delete`
  reads `FlagReferenceIndex`, appends a `FlagUpdatedEvent` (with `environmentIds` = set
  minus id, at current flag version) to each referencing flag, then appends
  `EnvironmentDeletedEvent`.
- **New** `VariantCommandHandler` — symmetric, appending a `FlagUpdatedEvent` with
  `variantIds` = set minus id.

### Controllers — `src/main/java/com/featureflags/controller/`
- `FeatureFlagCommandController` — `POST /flags`, `PATCH /flags/{flagId}`,
  `DELETE /flags/{flagId}`. **Remove** `PATCH /flags/{flagId}/name` and every
  `/environments` & `/variants` sub-route.
- **New** `EnvironmentController` — `POST /environments`, `PATCH /environments/{id}`,
  `GET /environments/{id}/usage` (returns `{environmentId, flags:[{flagId,name}]}`,
  names resolved by loading each flag stream), `DELETE /environments/{id}`.
- **New** `VariantController` — symmetric under `/variants`.

### Exceptions — `src/main/java/com/featureflags/exception/`
- `EnvironmentNotFoundException` / `VariantNotFoundException` — change to id-only messages.
- **New** `InvalidReferenceException extends FeatureFlagDomainException` for bad ids in a
  flag create/update.
- **Delete** `EnvironmentAlreadyExistsException` (name-uniqueness within a flag is gone).
- `GlobalExceptionHandler` — drop `EnvironmentAlreadyExistsException`; map
  `InvalidReferenceException` → `400 BAD_REQUEST`; keep not-found/gone/conflict/validation.

## Notes / trade-offs
- No read side exists, so there is no `GET /flags`; flag state is verified indirectly via
  the `usage` endpoints and HTTP status codes (acceptable for this command-only skeleton).
- Cascade strip on delete uses optimistic-concurrency appends per referencing flag; a
  concurrent edit surfaces as `OptimisticConcurrencyException` (409) — acceptable here.
- In-memory store starts empty, so the projection needs no startup replay (would replay
  if persistence is added later).

## Verification
Build & run (JDK 26 required):
```
export JAVA_HOME=/opt/homebrew/Cellar/openjdk/26.0.1/libexec/openjdk.jdk/Contents/Home
mvn -q clean package
java -jar target/feature-flags-0.1.0-SNAPSHOT.jar
```
Then exercise the new API with curl:
1. `POST /environments {"name":"prod","description":"production"}` → `201` + `environmentId`.
2. `POST /variants {"value":"true"}` → `201` + `variantId`.
3. `POST /flags {"name":"f1","environmentIds":["<envId>"],"variantIds":["<variantId>"]}` → `201` + `flagId`.
4. `POST /flags {"name":"bad","environmentIds":["<random-uuid>"]}` → `400` (invalid reference).
5. `GET /environments/{envId}/usage` → lists `f1`.
6. `PATCH /flags/{flagId} {"name":"f1-renamed","environmentIds":[]}` → `204`; usage now empty.
7. `DELETE /variants/{variantId}` → `204`; the flag's `variantIds` no longer contains it
   (confirm via `GET /variants/{variantId}/usage` → empty / 404).
8. `PATCH /flags/{flagId}/name` → `404/405` (route removed).
- `mvn -q test` is a no-op today (no tests); optionally add aggregate/handler unit tests
  and a `@SpringBootTest` slice if test coverage is desired (out of scope unless requested).
