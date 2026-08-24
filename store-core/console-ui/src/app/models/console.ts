import type {ProvisioningState, StoreStatus} from '@models/tenancy';

import type {IconName} from '@models/ui';
import type {Tone} from '@models/ui';

/**
 * The console chrome — navigation, stores, notifications and identity. Shared by every
 * page that renders inside the console shell, and independent of any one page's content.
 */

export interface NavigationItem {
  readonly labelKey: string;
  readonly icon: IconName;
  /** Absent while a section has no page yet — the item renders, but leads nowhere. */
  readonly route?: string;
  readonly badge?: string;
  readonly badgeTone?: Tone;
}

/**
 * Which of the two products a nav group belongs to.
 *
 * The console is two applications sharing one shell — a platform admin console and a merchant one —
 * and this is what keeps each operator's rail to the half they can use.
 *
 * **Both halves are exclusive, and the merchant half is the surprising one.** The platform pages are
 * super-admin only, so a merchant seeing them would get four screens that 403. Less obviously, the
 * *merchant* pages are unusable by a platform operator: every one is a reading of one store, the
 * store list they are handed is a truncated page of every tenant's rather than their own, and the
 * switcher is therefore hidden — so `?store=` resolves to a stranger's shop and the page 403s. See
 * lessons.md, "Shell — a super admin's store rail is the whole platform, truncated".
 *
 * A group with no audience is shown to everyone. Nothing uses that today; it is the honest default
 * for a section that genuinely belongs to both.
 */
export type NavigationAudience = 'platform' | 'merchant';

export interface NavigationSection {
  readonly groupKey: string;
  readonly items: readonly NavigationItem[];
  /**
   * Who this group is for. Omitted means everyone.
   *
   * It hides a group; it does not protect one. Every endpoint behind these pages carries its own
   * `@PreAuthorize`, and the `platformOnly` / `merchantOnly` route guards are what stop a typed URL
   * rendering a page that would fail row by row.
   */
  readonly audience?: NavigationAudience;
}

export interface ConsoleStore {
  readonly id: string;
  readonly name: string;
  /** Carried so the rail can mark a store that is still building, or that failed to. */
  readonly provisioningState: ProvisioningState;
  /** Only `ACTIVE` is operable; the rail dims the rest rather than hiding them. */
  readonly status: StoreStatus;
}

/**
 * The stores a user can switch between, and which one is open.
 *
 * There is no `defaultStoreId`: nothing on the backend can remember one — see lessons.md,
 * "Shell — no user-preferences endpoint".
 */
export interface StoreDirectory {
  readonly stores: readonly ConsoleStore[];
  /** Null while the account owns no store — the condition the first-run guards read. */
  readonly currentStoreId: string | null;
}

export interface ConsoleNotification {
  readonly id: string;
  readonly titleKey: string;
  readonly titleParams?: Record<string, string | number>;
  readonly detailKey: string;
  readonly detailParams?: Record<string, string | number>;
  readonly timeKey: string;
  readonly timeParams?: Record<string, string | number>;
  readonly icon: IconName;
  readonly tone: Tone;
  readonly unread: boolean;
}

export interface ConsoleUser {
  /** The person's name if uaa knows one, otherwise their username. */
  readonly name: string;
  readonly initials: string;
  /** Null today — uaa's ID token carries no email. The profile menu omits the line rather than faking it. */
  readonly email: string | null;
}
