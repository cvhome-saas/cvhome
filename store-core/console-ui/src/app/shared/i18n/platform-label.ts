import {Injectable, inject} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {humanizeStatus} from '@models/orders';
import {
  ORG_STATUSES,
  POD_HEALTH_STATUSES,
  POD_LIFECYCLE_STATES,
  POD_VISIBILITIES,
  PROVISIONING_STATES,
  STORE_STATUSES,
  SUBSCRIPTION_STATUSES,
} from '@models/platform';

/**
 * The server enums the platform console reads, in the reader's language.
 *
 * Not *only* the platform's: `provisioningState` is also what create-store shows a merchant, and
 * `subscriptionStatus` is what the merchant billing page shows. They are here because each is read
 * by two features, which is the rule for anything that would otherwise be duplicated — the tone maps
 * beside them in `@models/platform` are shared for the same reason.
 *
 * The same known-set guard `StatusLabel` and `RoleLabel` apply, for the same reason: Transloco is
 * configured to throw on a missing key, so a value added server-side must not be able to take a page
 * down. Every value the console has words for is listed in `@models/platform`; anything else is
 * humanized from its own name.
 *
 * **Its own namespace rather than a share of `status.*`.** The three order-side enums overlap in
 * meaning where they share a value — `PROCESSING` means one thing to a reader — and these do not:
 * a pod's `ACTIVE` and an organization's `ACTIVE` are different claims about different things, and
 * a store's `SUSPENDED` is a consequence of an organization's rather than the same fact.
 *
 * Every method reads `activeLang()` so a caller's `computed` re-runs on a language change — the
 * language is a dependency of the answer, not of the call.
 */
@Injectable({providedIn: 'root'})
export class PlatformLabel {
  private readonly transloco = inject(TranslocoService);

  /** An organization's status: Active, Suspended, Closed. */
  orgStatus(status: string | null | undefined): string {
    return this.lookup(ORG_STATUSES, status, (value) => `platform.orgStatus.${value}`);
  }

  /** Where a pod is in its operational life. */
  podLifecycle(state: string | null | undefined): string {
    return this.lookup(POD_LIFECYCLE_STATES, state, (value) => `platform.podLifecycle.${value}`);
  }

  /**
   * The last health probe's verdict, or "never probed".
   *
   * Null means nothing has asked *yet* rather than that nothing asks: `PodHealthProbe` sweeps every
   * minute, so a null is a pod registered since the last sweep. Only two of the three values can ever
   * arrive — the probe is a reachability check and answers GREEN or RED, never AMBER. See lessons.md,
   * "Pods — health history and audit are written and never read".
   */
  podHealth(status: string | null | undefined): string {
    if (!status) {
      this.transloco.activeLang();
      return this.transloco.translate('platform.pod.neverProbed');
    }
    return this.lookup(POD_HEALTH_STATUSES, status, (value) => `platform.podHealth.${value}`);
  }

  /** Whether a pod is shared or belongs to one organization. */
  podVisibility(visibility: string | null | undefined): string {
    return this.lookup(POD_VISIBILITIES, visibility, (value) => `platform.podVisibility.${value}`);
  }

  /** Whether a store may be used at all — an operator's lever. */
  storeStatus(status: string | null | undefined): string {
    return this.lookup(STORE_STATUSES, status, (value) => `shared.storeStatus.${value}`);
  }

  /** How far a store got in being built. Unrelated to whether it is paid for. */
  provisioningState(state: string | null | undefined): string {
    return this.lookup(PROVISIONING_STATES, state, (value) => `shared.provisioningState.${value}`);
  }

  /**
   * Whether a store is paid for. Mirrors billing's `SubscriptionStatus`.
   *
   * **Null is "unknown", not "lapsed".** `ManagerStoreDto.billingStatus` is read from billing by
   * tenancy, and comes back null both when billing has no subscription for that store and when
   * billing could not be reached — `InternalStoreServiceImpl.withBillingStatus` fails open on any
   * failure, deliberately. The em dash the guard returns is the right answer for either: neither is
   * a reason to tell an operator a merchant has stopped paying.
   */
  subscriptionStatus(status: string | null | undefined): string {
    return this.lookup(SUBSCRIPTION_STATUSES, status, (value) => `shared.subscriptionStatus.${value}`);
  }

  /**
   * The known-set guard, with the key built by the caller.
   *
   * The caller passes a template literal rather than a namespace string, and that is not a style
   * choice: `npm run lint:i18n` reads the *static head* of every composed key out of the source to
   * decide which translations are reachable. A key assembled from a parameter has no static head, so
   * every value in all six namespaces would be reported as dead and deleted — taking the six enums
   * down under the strict missing-key handler, which throws rather than falling back.
   */
  private lookup(
    known: ReadonlySet<string>,
    value: string | null | undefined,
    key: (value: string) => string,
  ): string {
    this.transloco.activeLang();
    if (!value) {
      return '—';
    }
    return known.has(value) ? this.transloco.translate(key(value)) : humanizeStatus(value);
  }
}
