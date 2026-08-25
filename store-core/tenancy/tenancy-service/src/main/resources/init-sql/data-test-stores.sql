-- Fixtures for the API integration tests, seeded only under the `test-stores` profile.
--
-- Each class that mutates an organization owns its own row here. Sharing data.sql's two organizations would make
-- the lifecycle tests order-dependent: closing an organization is terminal, and every store-listing test reads
-- those same two.

-- Renamed, suspended and resumed by OrgManagerApiIntegrationTest.
INSERT INTO tenancy.manager_org(id, created_date, email, name, status, version)
VALUES ('11111111111111111111aa01', CURRENT_DATE, 'lifecycle@example.com', 'Lifecycle Org', 'ACTIVE', 1)
ON CONFLICT DO NOTHING;

-- Already closed: CLOSED is terminal, so every transition off it must be refused.
INSERT INTO tenancy.manager_org(id, created_date, email, name, status, version)
VALUES ('11111111111111111111aa02', CURRENT_DATE, 'closed@example.com', 'Closed Org', 'CLOSED', 1)
ON CONFLICT DO NOTHING;

-- The only organization with a recorded owner: change-password resolves the uaa user through this column, and
-- answers 422 for the organizations that have none.
INSERT INTO tenancy.manager_org(id, created_date, email, name, status, version, owner_user_id)
VALUES ('11111111111111111111aa03', CURRENT_DATE, 'owned@example.com', 'Owned Org', 'ACTIVE', 1,
        'b0a4f3d2-0000-4000-8000-000000000001')
ON CONFLICT DO NOTHING;

-- Members and invitations live on their own organizations so the two OrgMemberApi tenants cannot see each other.
INSERT INTO tenancy.manager_org(id, created_date, email, name, status, version)
VALUES ('11111111111111111111aa04', CURRENT_DATE, 'members@example.com', 'Members Org', 'ACTIVE', 1)
ON CONFLICT DO NOTHING;

INSERT INTO tenancy.manager_org(id, created_date, email, name, status, version)
VALUES ('11111111111111111111aa05', CURRENT_DATE, 'neighbour@example.com', 'Neighbour Org', 'ACTIVE', 1)
ON CONFLICT DO NOTHING;

-- The organization store creation is charged to; it starts with no stores so a created one is unambiguous.
INSERT INTO tenancy.manager_org(id, created_date, email, name, status, version)
VALUES ('11111111111111111111aa06', CURRENT_DATE, 'creator@example.com', 'Creator Org', 'ACTIVE', 1)
ON CONFLICT DO NOTHING;

-- A suspended organization: its stores are refused entry even though the store rows themselves are ACTIVE.
INSERT INTO tenancy.manager_org(id, created_date, email, name, status, version)
VALUES ('11111111111111111111aa07', CURRENT_DATE, 'suspended@example.com', 'Suspended Org', 'SUSPENDED', 1)
ON CONFLICT DO NOTHING;

INSERT INTO tenancy.manager_store(id, name, created_date, org_id, pod_id, provisioning_state, status, version)
VALUES ('11111111111111111111bb01', 'MEMBERS-STORE', CURRENT_DATE, '11111111111111111111aa04',
        '507f1f77bcf86cd799439011', 'SUCCESSFULLY_PROVISIONING', 'ACTIVE', 1)
ON CONFLICT DO NOTHING;

INSERT INTO tenancy.manager_store(id, name, created_date, org_id, pod_id, provisioning_state, status, version)
VALUES ('11111111111111111111bb02', 'SUSPENDED-ORG-STORE', CURRENT_DATE, '11111111111111111111aa07',
        '507f1f77bcf86cd799439011', 'SUCCESSFULLY_PROVISIONING', 'ACTIVE', 1)
ON CONFLICT DO NOTHING;

-- A store that is itself suspended, on an otherwise healthy organization.
INSERT INTO tenancy.manager_store(id, name, created_date, org_id, pod_id, provisioning_state, status, version)
VALUES ('11111111111111111111bb03', 'SUSPENDED-STORE', CURRENT_DATE, '11111111111111111111aa01',
        '507f1f77bcf86cd799439011', 'SUCCESSFULLY_PROVISIONING', 'SUSPENDED', 1)
ON CONFLICT DO NOTHING;

-- Soft-deleted: every listing has to leave it out.
INSERT INTO tenancy.manager_store(id, name, created_date, org_id, pod_id, provisioning_state, status, version)
VALUES ('11111111111111111111bb04', 'DELETED-STORE', CURRENT_DATE, '11111111111111111111aa01',
        '507f1f77bcf86cd799439011', 'SUCCESSFULLY_PROVISIONING', 'DELETED', 1)
ON CONFLICT DO NOTHING;

INSERT INTO tenancy.org_member(org_id, user_id, role, added_at, added_by)
VALUES ('11111111111111111111aa04', 'member-one', 'STORE_ADMIN', now(), 'seed')
ON CONFLICT DO NOTHING;

INSERT INTO tenancy.org_member(org_id, user_id, role, added_at, added_by)
VALUES ('11111111111111111111aa05', 'neighbour-member', 'STORE_ADMIN', now(), 'seed')
ON CONFLICT DO NOTHING;
