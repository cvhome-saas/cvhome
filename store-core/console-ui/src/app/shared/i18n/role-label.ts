import {Injectable, inject} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {KNOWN_ROLES} from '@models/team';
import {humanizeStatus} from '@models/orders';

/**
 * A role name, in the reader's language.
 *
 * The same known-set guard `StatusLabel` applies to the server's status enums, applied to its role
 * table. uaa's `uaa.roles` is a table, not an enum — a row can be added with
 * `POST /api/v1/admin/roles` and would then arrive here — and Transloco is configured to throw on a
 * missing key, so looking one up blind would let a role created in a database take the user list
 * down. Every role the console has words for is in `KNOWN_ROLES`; anything else is humanized from
 * its own name.
 *
 * A separate `role.*` namespace rather than a share of `status.*`: the three status enums overlap in
 * meaning where they share a value, and roles overlap with none of them.
 */
@Injectable({providedIn: 'root'})
export class RoleLabel {
  private readonly transloco = inject(TranslocoService);

  /**
   * Reads `activeLang` so a caller's `computed` re-runs on a language change — the language is a
   * dependency of the answer, not of the call.
   */
  label(role: string | null | undefined): string {
    this.transloco.activeLang();
    if (!role) {
      return '—';
    }
    return KNOWN_ROLES.has(role) ? this.transloco.translate(`role.${role}`) : humanizeStatus(role);
  }

  /** Several roles, joined the way a list is written in the reader's language. */
  labels(roles: readonly string[] | null | undefined): string {
    this.transloco.activeLang();
    if (!roles?.length) {
      return '—';
    }
    return roles.map((role) => this.label(role)).join(this.transloco.translate('shared.listSeparator'));
  }
}
