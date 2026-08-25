-- Drops the duplicate unique indexes Hibernate generated in the `content` schema.
--
-- WHY THEY EXIST
--
-- `init-sql/schema.sql` declares each unique constraint by name (`content_store_code_unique`, …), but the JPA
-- entities used to declare the same constraints without a `name`. Under `ddl-auto: update` Hibernate cannot
-- match an unnamed constraint to an existing one, so it created a second index (`ukbm9y7874o2jvipd27cixy6hig`
-- and friends) beside every one of ours — eleven redundant indexes, each paid for on every insert and update.
--
-- The entities now carry the DDL's names, so no new duplicates appear. This file removes the ones already
-- created. It is not part of `schema.sql` because Spring's script runner splits on `;` and cannot parse a
-- `do $$ … $$` block.
--
-- ORDERING: safe at any time, before or after deploying the release that names the constraints. Dropping a
-- redundant unique index cannot lose a constraint — the identically-shaped named one stays.
--
-- Run against the content database:
--
--     psql "$CONTENT_DB_URL" -f 2026-08-23-content-drop-duplicate-unique-indexes.sql

do
$$
    declare
        dup record;
    begin
        for dup in select t.relname as table_name, i.relname as index_name
                   from pg_index x
                            join pg_class i on i.oid = x.indexrelid
                            join pg_class t on t.oid = x.indrelid
                            join pg_namespace n on n.oid = t.relnamespace
                   where n.nspname = 'content'
                     and x.indisunique
                     and i.relname ~ '^uk[a-z0-9]{20,}$'
            loop
                raise notice 'dropping % on %', dup.index_name, dup.table_name;
                execute format('alter table content.%I drop constraint if exists %I', dup.table_name,
                               dup.index_name);
                execute format('drop index if exists content.%I', dup.index_name);
            end loop;
    end
$$;

-- verify: expect zero rows
select tablename, indexname
from pg_indexes
where schemaname = 'content'
  and indexname ~ '^uk[a-z0-9]{20,}$';
