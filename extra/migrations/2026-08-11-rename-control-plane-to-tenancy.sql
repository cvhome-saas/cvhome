-- Renames control-plane's two Postgres schemas to match the service's new name, tenancy.
--
-- ORDERING — this is the whole risk of this migration:
--
--     1. stop the old control-plane instance(s)
--     2. run this file
--     3. deploy the tenancy release
--
-- Both statements are catalog-only DDL: instantaneous, and they preserve every row, including outbox records that
-- are mid-flight. There is no drain, no dual write and no data copy. What is *not* safe is running them against a
-- live instance, or deploying either release on the wrong side of them. A running control-plane holds "manager" and
-- "control" in its pinned @Table annotations and its Hikari search_path; the tenancy release holds "tenancy" and
-- "tenancy_outbox". Whichever one is running while the other's names are in the database fails with
-- `relation "..." does not exist`.
--
-- THE SILENT FAILURE MODE, if this file is skipped entirely:
--
-- tenancy boots with `spring.sql.init.mode: always`, so its schema.sql will happily `create schema if not exists
-- tenancy` and create *empty* manager_org / manager_store tables next to the untouched, still-populated `manager`
-- ones. Nothing errors. The console simply shows no orgs and no stores, and every provisioned tenant looks gone.
-- If that happens: stop the service, drop the empty `tenancy` schema, run this file, start it again.
--
-- WHAT CHANGES SHAPE
--
-- Nothing. This is a rename of two schema *containers*. Table names, columns, constraints, indexes and every row
-- are untouched — `manager.manager_store` becomes `tenancy.manager_store`, same table. The entity type names
-- (ManagerOrgEntity, ManagerStoreEntity) and the `manager_*` table names deliberately survive the service rename;
-- see the plan's §0a for why (`ManagerStoreId` is matched by literal name in @PreAuthorize expressions repo-wide).
--
-- The `org` schema is deliberately NOT renamed here. It holds only `org.pod`, which moves to its own service in a
-- later phase of the split; renaming it now would mean migrating it twice.

alter schema manager rename to tenancy;
alter schema control rename to tenancy_outbox;

-- ---------------------------------------------------------------------------------------------------------------
-- The outbox rows need more than a schema rename — they name Java classes that this release renames.
-- ---------------------------------------------------------------------------------------------------------------
--
-- namastack stores `record_type` and `handler_id` as fully-qualified class names, e.g.
--
--   record_type  com.asrevo.cvhome.controlplane.manager.commons.event.store.StoreCreatedEvent
--   handler_id   com.asrevo.cvhome.controlplane.manager.processors.event.ManagerStoreCreatedEventImpl
--                  #process(com.asrevo.cvhome.controlplane.manager.commons.event.store.StoreCreatedEvent)
--
-- Those classes do not exist after this release, so ALTER SCHEMA alone is not enough: it preserves the row
-- perfectly while leaving it pointing at a type and a handler that cannot be resolved.
--
-- What was actually verified locally, and what was not, because the difference matters if you are relying on
-- this: the FQNs above were read straight out of `outbox_record` on a running stack, and application-generated
-- events were confirmed to flow end to end after the rename. The *failure* of an old-FQN row was NOT
-- reproduced — hand-inserted probe rows are not picked up by the poller at all (rows with untouched, valid
-- FQNs sat PENDING exactly like the rewritten ones), so that experiment cannot distinguish. The rewrite below
-- is therefore a cheap precaution justified by inspection, not a fix for an observed failure. It was checked
-- to produce values byte-identical to the ones the new release writes.
--
-- Only PENDING rows would need it, but COMPLETED rows are rewritten too so the table stays queryable by type.

update tenancy_outbox.outbox_record
   set record_type = replace(record_type,
                             'com.asrevo.cvhome.controlplane.manager.commons.event.store.',
                             'com.asrevo.cvhome.tenancy.events.store.'),
       handler_id  = replace(replace(handler_id,
                             'com.asrevo.cvhome.controlplane.manager.commons.event.store.',
                             'com.asrevo.cvhome.tenancy.events.store.'),
                             'com.asrevo.cvhome.controlplane.manager.processors.event.',
                             'com.asrevo.cvhome.tenancy.manager.processors.event.')
 where record_type like 'com.asrevo.cvhome.controlplane.%'
    or handler_id  like '%com.asrevo.cvhome.controlplane.%';

-- Belt and braces: the safest deploy still drains the outbox first. With the old release stopped, this should
-- report zero before you start the new one. If it does not, the rewrite above is what carries those rows over.
--
--   select status, count(*) from tenancy_outbox.outbox_record where status = 'PENDING' group by status;

-- Verify before starting the new release. Expect the two schemas to exist with their tables and their full row
-- counts, and expect no leftovers under the old names.
--
--   select table_schema, table_name from information_schema.tables
--    where table_schema in ('tenancy', 'tenancy_outbox', 'manager', 'control') order by 1, 2;
--   select count(*) from tenancy.manager_org;
--   select count(*) from tenancy.manager_store;
--   -- outbox continuity: anything still pending must survive the rename and be picked up after restart
--   select status, count(*) from tenancy_outbox.outbox_record group by status;
