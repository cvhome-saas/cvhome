import {Injectable, computed, inject, linkedSignal, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import type {PodRow} from '@models/platform';
import {snapshot} from '@shared/state/snapshot';
import {PodsApi} from '../services/pods.api.service';

export const PAGE_SIZE = 20;

/**
 * The pod fleet.
 *
 * Nothing here is store-scoped. The search is the registry's own — `?q=` matches a pod's name and
 * its endpoint, which are the two things an operator has in hand when they come looking; the id is
 * an ObjectId nobody reads out, so it is not in the predicate.
 */
@Injectable()
export class PodsFacade {
  private readonly api = inject(PodsApi);
  private readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);

  /** The search term, as sent to the registry. */
  readonly search = signal('');

  /** Back to the first page whenever the term changes — page 4 of a smaller fleet is nothing. */
  readonly pageIndex = linkedSignal<unknown, number>({
    source: () => this.search(),
    computation: () => 0,
  });

  private readonly pods = snapshot(
    () => ({page: this.pageIndex(), count: PAGE_SIZE, term: this.search()}),
    (query) => this.api.loadPods(query),
  );

  readonly isLoading = this.pods.isLoading;
  readonly error = this.pods.error;
  readonly isEmpty = this.pods.isEmpty;
  readonly reload = () => this.pods.reload();

  readonly rows = computed<readonly PodRow[]>(() => this.pods.value()?.rows ?? []);
  readonly totalElements = computed(() => this.pods.value()?.totalElements ?? 0);
  readonly totalPages = computed(() => this.pods.value()?.totalPages ?? 0);

  /** Whether the rows on screen answer the term in the box. See the customers page for the failure. */
  readonly rowsMatchSearch = computed(() => this.pods.value()?.term === this.search());

  readonly heading = computed(() => {
    this.transloco.activeLang();
    return {
      title: this.transloco.translate('platform.pods.heading.title'),
      context: this.transloco.translate('platform.pods.heading.context'),
    };
  });

  readonly totalLabel = computed<string | null>(() => {
    if (this.isEmpty()) {
      return null;
    }
    this.transloco.activeLang();
    return this.transloco.translate('platform.pods.totalLabel', {
      total: this.localeFormat.localizeNumber(this.totalElements(), 'decimal'),
      count: this.totalElements(),
    });
  });

  readonly subtitle = computed(() => {
    this.transloco.activeLang();
    const shown = this.rows().length;
    if (!shown) {
      return this.transloco.translate('platform.pods.subtitle.none');
    }
    const digits = (value: number) => this.localeFormat.localizeNumber(value, 'decimal');
    const from = this.pageIndex() * PAGE_SIZE + 1;
    return this.transloco.translate('platform.pods.subtitle.range', {
      from: digits(from),
      to: digits(from + shown - 1),
      total: digits(this.totalElements()),
      count: this.totalElements(),
    });
  });

  /**
   * The owning organization, named where the lookup reached it.
   *
   * A shared pod has no owner at all, which is a different answer from "an owner whose name we could
   * not resolve" — the first reads as *Public*, the second falls back to the id.
   */
  ownerLabel(orgId: string | null): string {
    this.transloco.activeLang();
    if (!orgId) {
      return this.transloco.translate('platform.pods.public');
    }
    return this.pods.value()?.orgNames.get(orgId) ?? orgId;
  }

  /**
   * How many stores are on a pod, in the reader's numerals.
   *
   * An em dash for null, which is "the count could not be read" — a pod with no stores answers zero, and the two
   * are different things to tell an operator about to drain or delete one.
   */
  storeCount(stores: number | null): string {
    this.transloco.activeLang();
    return stores === null ? '—' : this.localeFormat.localizeNumber(stores, 'decimal');
  }

  setSearch(term: string): void {
    this.search.set(term);
  }

  goToPage(page: number): void {
    this.pageIndex.set(page);
  }
}
