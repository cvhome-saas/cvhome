import {Component, inject} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {PlatformUserRow, SessionSummary, UserAdminAction, UserAdminIntent} from '@cvhome-saas/ui-kit/uaa';
import {UserAdminTable} from '@cvhome-saas/ui-kit/uaa';
import {
  BusyOverlay,
  ConfirmDialog,
  EmptyState,
  FormDialog,
  LoadError,
  PageHeader,
  Pagination,
  Panel,
  SearchBox,
  Select,
} from '@cvhome-saas/ui-kit/ui';
import {PAGE_SIZE, ShoppersFacade} from './facades/shoppers.facade';
import {SHOPPERS_PROVIDERS} from './services/shoppers.api.service';

/**
 * The people who sign in to this store.
 *
 * Not the customers page: that lists who has ordered, which is checkout's record of a person. This
 * lists who has an account, which is cua's — and the two are not the same set. A guest checkout
 * leaves a customer and no account; a shopper who registered and never bought is here and not there.
 *
 * The actions are the ones a merchant actually has. There is no password reset and no role editing
 * because cua offers neither for a shopper: those accounts self-register, their role is the
 * deployment's configuration, and a password is reset from the storefront by the person who owns it.
 * The table is told what this console can do rather than assuming.
 */
@Component({
  selector: 'app-shoppers',
  imports: [
    BusyOverlay,
    ConfirmDialog,
    EmptyState,
    FormDialog,
    LoadError,
    PageHeader,
    Pagination,
    Panel,
    SearchBox,
    Select,
    TranslocoDirective,
    UserAdminTable,
  ],
  providers: [...SHOPPERS_PROVIDERS, ShoppersFacade],
  templateUrl: './shoppers.html',
  styleUrl: './shoppers.css',
})
export class Shoppers {
  private readonly localeFormat = inject(TranslocoLocaleService);

  protected readonly facade = inject(ShoppersFacade);

  protected readonly pageSize = PAGE_SIZE;

  /** What cua lets a merchant do to a shopper, and therefore what the row menu offers. */
  protected readonly allowed: readonly UserAdminAction[] = ['unlock', 'toggleEnabled', 'delete'];

  /* Bound once as a field: a method reference created in a binding is a new function every tick. */
  protected readonly roleList = () => '';

  protected onAction(intent: UserAdminIntent): void {
    switch (intent.kind) {
      case 'toggleEnabled':
        this.facade.toggleEnabled(intent.row);
        break;
      case 'unlock':
        this.facade.unlock(intent.row);
        break;
      case 'delete':
        this.facade.deleting.set(intent.row);
        break;
      default:
        // resetPassword and editRoles are not offered; the table filters them out.
        break;
    }
  }

  protected onPick(row: PlatformUserRow): void {
    this.facade.inspect(row);
  }

  protected nameOf(row: PlatformUserRow | null): string {
    return row?.username ?? '';
  }

  /** The reader's own clock and calendar; the wire carries UTC ISO-8601, which is nobody's. */
  protected started(session: SessionSummary): string {
    return this.localeFormat.localizeDate(session.createdAt, undefined, {dateStyle: 'medium', timeStyle: 'short'});
  }
}
