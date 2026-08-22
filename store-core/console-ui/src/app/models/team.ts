import type {InvitationStatus} from '@models/users';
import type {KpiDatum, Tone} from '@models/ui';

/**
 * The user-management page's view models.
 *
 * The wire shapes live in `@models/users`; this is what the page renders — the `checkout.ts` /
 * `orders.ts` split, applied again.
 *
 * What is **not** here is most of the design. `console-template/User Management.dc.html` draws an
 * avatar photo, a phone number, a postal address, a "last active" column, a lifetime value, an order
 * tally per person and a running count of admin actions. A uaa user is eight columns —
 * `{id, username, email, first_name, last_name, password_hash, metadata, enabled}` — and none of
 * those is one of them. See lessons.md, the Users entries.
 *
 * The template also merges staff and customers into a single list. They come from two different
 * services with no key in common, and the customer half is Module 9; this page is the team.
 */

/** The two things the page shows: who is here, and who has been asked. */
export type UsersTab = 'team' | 'invitations';

export const USERS_TABS: readonly UsersTab[] = ['team', 'invitations'];

/**
 * The roles the console will offer, in the order a picker should list them — widest last.
 *
 * **`SUPER_ADMIN` is deliberately absent.** `GET …/assignable-roles` returns uaa's whole role table
 * minus `USER` and `ORG_ADMIN`, which leaves platform superuser in the list handed to an org admin.
 * The console intersects the server's answer with this set, so a role the server invents cannot
 * appear in a picker unreviewed and the one it should not be offering cannot appear at all. That is
 * defence in depth and not a fix — see lessons.md, "Users — assignable-roles offers SUPER_ADMIN to
 * an org admin".
 */
export const OFFERABLE_ROLES: readonly string[] = ['STORE_MODERATOR', 'STORE_ADMIN'];

/**
 * The roles the console has words for.
 *
 * Transloco throws on a missing key, so a role name added server-side would take the page down if it
 * were looked up blind. Anything outside this set is humanized instead — the same known-set guard
 * Module 4 established for order statuses, applied to a different server enum.
 */
export const KNOWN_ROLES: ReadonlySet<string> = new Set<string>([
  'SUPER_ADMIN',
  'ORG_ADMIN',
  'STORE_ADMIN',
  'STORE_MODERATOR',
  'STORE_RETAIL',
  'USER',
]);

/**
 * The invitation statuses the console has words for.
 *
 * A Java enum rather than a database table, so unlike a role it cannot grow underneath the console
 * — but it goes through the same known-set guard anyway, because Transloco throws on a missing key
 * and a fifth value would take the tab down rather than render oddly.
 */
export const INVITATION_STATUSES: ReadonlySet<string> = new Set<string>([
  'PENDING',
  'ACCEPTED',
  'REVOKED',
  'EXPIRED',
]);

/** Invitation status to its categorical tone, consistent with the rest of the console's badges. */
export const INVITATION_TONE: Readonly<Record<InvitationStatus, Tone>> = {
  PENDING: 'amber',
  ACCEPTED: 'green',
  REVOKED: 'slate',
  EXPIRED: 'slate',
};

/** One row of the team table. Every field is read off the user the list already returned. */
export interface TeamRow {
  /** uaa's UUID. The track-by, and the subject of every write on this page. */
  readonly id: string;
  /** First and last name joined, or the username when neither is set — never blank. */
  readonly name: string;
  /** Kept apart as well as joined: the edit form has a field for each, and splitting `name` on a
   *  space would mangle anyone whose given name has one in it. */
  readonly firstName: string;
  readonly lastName: string;
  /** The username, shown beside the name because it is what a person signs in with. */
  readonly userName: string;
  readonly email: string;
  /** The server's role names, unlabelled — the page translates them through the known-set guard. */
  readonly roles: readonly string[];
  readonly active: boolean;
  /**
   * The store this account is confined to, or null for an org-level one.
   *
   * Null rows do not appear in this list at all — `user-account/list` filters on `{org, store}` —
   * so this is here to be honest about the field rather than because the page can render both. See
   * lessons.md, "Users — the user list is store-scoped, so an org admin is in no list".
   */
  readonly store: string | null;
  /** Whether this row is the signed-in operator, matched on username since the JWT carries no id. */
  readonly isSelf: boolean;
}

/** One row of the invitations table. */
export interface InvitationRow {
  readonly id: string;
  readonly email: string;
  readonly role: string;
  readonly status: InvitationStatus;
  readonly tone: Tone;
  /** ISO instants, formatted by the page so they survive a language change. */
  readonly expiresAt: string;
  readonly createdAt: string;
  readonly createdBy: string;
  /** Only a pending invitation can be resent or revoked. */
  readonly pending: boolean;
}

/**
 * A newly issued invitation, held just long enough to be copied.
 *
 * The token exists in plaintext exactly once, in the response that created it — only its hash is
 * stored — so this is not something the page can fetch again. Losing it means issuing a new
 * invitation. Nothing on the platform sends email, which is why the operator is handed a link
 * instead of a confirmation. See lessons.md, "Users — nothing emails an invitation".
 */
export interface IssuedInvitation {
  readonly email: string;
  readonly role: string;
  /** The absolute link to hand over, already assembled against the console's own origin. */
  readonly link: string;
  readonly expiresAt: string;
}

/** One KPI's source data, resolved into a `KpiDatum` by the facade. */
export interface TeamKpiSource {
  readonly labelKey: string;
  /** Null when the figure could not be read — an em dash under a flag, never a zero. */
  readonly value: string | null;
  readonly icon: KpiDatum['icon'];
  readonly tone: KpiDatum['tone'];
  readonly flagKey?: string;
}
