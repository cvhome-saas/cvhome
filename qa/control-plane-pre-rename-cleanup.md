# QA — control-plane pre-rename cleanup (phase 0)

Phase 0 of `.claude/plans/tenancy-and-pod-registry-split.md`. Four changes, none of which is meant to alter
behaviour: an empty module deleted, two BOM versions hoisted into the version catalog, a dead class deleted,
and a duplicated converter registration removed from `JdbcConfig`.

**Only one of the four can break anything at runtime**, and it is the one worth testing: `JdbcConfig`
registered `Identifier -> String` and `String -> ManagerOrgId` twice, verbatim. Removing the second pair is
safe *if* Spring Data JDBC was not relying on the duplicate registration order. If it were wrong, every org
and store id would fail to convert and the console's store screens would 500 or come back empty. Everything
else in this phase is build-time only — if the build passes, it is correct.

## Setup

```bash
./extra/scripts/run-lcl.sh          # background it; stop with SIGTERM, never SIGINT
```

Sign in at `http://gateway.com:8000/` as `org1-admin` / `admin` (seed accounts, `test-stores` profile only).

## Cases

### 1. Store list still resolves ids — **run, passed**

The one case that actually exercises the changed code.

1. Sign in as `org1-admin`.
2. Store management → Stores list.

**Expect:** two rows, `ORG1-STORE1` and `ORG1-STORE2`, each showing a non-empty **ID** column
(`65f023632bc46470c104b76f`, `…75f`), a **Pod Id** of `507f1f77bcf86cd799439011`, and status
`SUCCESSFULLY_PROVISIONING`. A populated ID *and* Pod Id column is the actual assertion — those three columns
are `ManagerStoreId`, `ManagerOrgId` (implicit, via the scoping) and `PodId` coming back through the
converters that were de-duped. Blank ids, an empty table, or a 500 would mean the removal was not safe.

Also confirm the store selector in the header reads `ORG1-STORE1` — it is populated by the same call.

### 2. Build gates — **run, passed**

```bash
./gradlew checkstyleMain checkstyleTest    # warnings = errors
./gradlew build -x test -x check
```

The second one is what proves the `manager-external-api` deletion and the `libs.versions.toml` hoist are
complete: a missed `settings.gradle` entry or a bad catalog key fails configuration, not compilation, so it
surfaces immediately.

## Not covered, deliberately

- **Tenant isolation and the permission gate are not re-proven here.** No endpoint, permission token or query
  changed in this phase. They are re-proven in phase 1, where every path moves.
- No migration, no config change, so there is no deploy-ordering concern for this phase.
