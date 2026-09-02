/*
 * @cvhome-saas/ui-kit/uaa — clients for uaa's admin API, and the view models that go with them.
 *
 * The one entry point here that names a domain rather than a capability, and it earns that: both
 * consoles administer the same uaa, so without it the second one would re-implement the client the
 * first already has — which is the duplication this library exists to remove.
 *
 * All three APIs sit behind `SCOPE_super_admin`/`ROLE_SUPER_ADMIN`, enforced by uaa's own filter
 * chain and repeated on every method. The gateway relays the operator's token unchanged; uaa's
 * guard, not the gateway's, is what keeps them safe.
 */
export * from './lib/uaa.models';
export * from './lib/user-row';
export * from './lib/admin-user.service';
export * from './lib/admin-role.service';
export * from './lib/admin-settings.service';
export * from './lib/account.service';
export * from './lib/admin-client.service';
export * from './lib/user-admin-table/user-admin-table';
