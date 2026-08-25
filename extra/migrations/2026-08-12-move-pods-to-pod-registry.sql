-- Moves the pod registry out of tenancy's `org` schema and into pod-registry's own.
--
-- ORDERING:
--
--     1. deploy pod-registry (phase 4) and let it start at least once — its PodSeedInitializer creates
--        pod_registry.pod and fills in every pod that appears in ServiceDomainProperties
--     2. run part 1 of this file, with tenancy stopped or already on the new release
--     3. verify (query at the bottom), then run part 2
--
-- WHY A COPY IS STILL NEEDED, given pod-registry already seeds itself from configuration.
--
-- Configuration knows a pod's id, name and endpoint. It does NOT know two things that only ever existed in the
-- database: `org_id`, the private-pod assignment that decides which organization may be placed on a pod, and any
-- pod an operator created through the old POST /api/v1/pod. Booting pod-registry gets you the pods; only this file
-- gets you who owns them. Skipping it does not fail loudly — placement simply treats a formerly-private pod as
-- shared, and starts putting other organizations' stores on dedicated infrastructure. That is the exact bug this
-- phase of work exists to remove, so it would be an unusually cruel way to reintroduce it.

-- ---------------------------------------------------------------------------------------------------------------
-- Part 1 — copy. Safe to re-run.
-- ---------------------------------------------------------------------------------------------------------------

-- Guarded so this file still parses and runs cleanly after part 2 has dropped the source.
DO
$$
    BEGIN
        IF to_regclass('org.pod') IS NULL THEN
            RAISE NOTICE 'org.pod does not exist; nothing to copy (already migrated, or a fresh install)';
            RETURN;
        END IF;

        -- ON CONFLICT DO NOTHING, because pod-registry has usually already seeded these rows from configuration
        -- and its copy is the newer one. The columns that seeding could not know are filled in below instead.
        INSERT INTO pod_registry.pod (id, name, endpoint, endpoint_type, org_id, visibility, lifecycle_state,
                                      capacity_stores, version)
        SELECT p.id,
               p.name,
               p.endpoint,
               p.endpoint_type,
               p.org_id,
               CASE WHEN p.org_id IS NULL THEN 'PUBLIC' ELSE 'PRIVATE' END,
               'ACTIVE',
               0,
               COALESCE(p.version, 1)
        FROM org.pod p
        ON CONFLICT (id) DO NOTHING;

        -- The part that actually matters. A pod seeded from configuration arrives PUBLIC with no owner, because
        -- configuration has no notion of one; this restores the private assignment for pods that had it. The
        -- pod_private_owner_ck constraint requires both columns to move together, so they are set together.
        UPDATE pod_registry.pod r
        SET org_id     = p.org_id,
            visibility = 'PRIVATE'
        FROM org.pod p
        WHERE r.id = p.id
          AND p.org_id IS NOT NULL
          AND r.org_id IS DISTINCT FROM p.org_id;
    END
$$;

-- ---------------------------------------------------------------------------------------------------------------
-- Verify before running part 2. Every row on the left must have a counterpart on the right, org_id included.
-- ---------------------------------------------------------------------------------------------------------------

-- SELECT p.id, p.name, p.org_id AS was_owned_by, r.org_id AS now_owned_by, r.visibility
-- FROM org.pod p
--          LEFT JOIN pod_registry.pod r ON r.id = p.id
-- ORDER BY p.name;

-- ---------------------------------------------------------------------------------------------------------------
-- Part 2 — DESTRUCTIVE. Run only once the query above shows every pod carried over with the right owner.
--
-- Kept separate on purpose: part 1 is idempotent and reversible by doing nothing, this is neither. There is no
-- foreign key from tenancy.manager_store.pod_id into either table, so dropping the source does not cascade — but
-- it is also the only remaining copy of the old assignments if part 1 was wrong.
-- ---------------------------------------------------------------------------------------------------------------

-- DROP SCHEMA org CASCADE;
