import {Component, computed, inject, input, output} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {TranslocoDecimalPipe, TranslocoLocaleService} from '@jsverse/transloco-locale';

import {Icon} from '../icon/icon';

/** How many numbered buttons the strip shows before it starts eliding. */
const WINDOW = 5;

/**
 * Page controls for a record list: a count of what is on screen, and a windowed strip of
 * page numbers.
 *
 * Pages are zero-based on the wire, matching `PageRequest`, and one-based on screen. The
 * translation happens here so no caller has to remember which convention it is holding.
 *
 * The strip keeps the current page centred and always shows the first and last page, so
 * the ends of a long list stay one click away.
 */
@Component({
  selector: 'app-pagination',
  imports: [Icon, TranslocoDecimalPipe],
  template: `
    <p class="page-info">{{ summary() }}</p>

    @if (totalPages() > 1) {
      <nav class="page-strip" [attr.aria-label]="resolvedLabel()">
        <button
          class="page-step"
          type="button"
          [attr.aria-label]="transloco.translate('shared.pagination.previousPage')"
          [disabled]="page() === 0"
          (click)="go(page() - 1)"
        >
          <app-icon name="chevronLeft" [flip]="true" />
        </button>

        @for (slot of slots(); track $index) {
          @if (slot === null) {
            <span class="page-gap" aria-hidden="true">…</span>
          } @else {
            <button
              class="page-number"
              type="button"
              [class.current]="slot === page()"
              [attr.aria-label]="transloco.translate('shared.pagination.pageN', {n: slot + 1})"
              [attr.aria-current]="slot === page() ? 'page' : null"
              (click)="go(slot)"
            >
              {{ slot + 1 | translocoDecimal }}
            </button>
          }
        }

        <button
          class="page-step"
          type="button"
          [attr.aria-label]="transloco.translate('shared.pagination.nextPage')"
          [disabled]="page() >= totalPages() - 1"
          (click)="go(page() + 1)"
        >
          <app-icon name="chevronRight" [flip]="true" />
        </button>
      </nav>
    }
  `,
  styleUrl: './pagination.css',
})
export class Pagination {
  protected readonly transloco = inject(TranslocoService);
  private readonly localeFormat = inject(TranslocoLocaleService);

  /** Zero-based, as `PageRequest` carries it. */
  readonly page = input.required<number>();
  readonly totalPages = input.required<number>();
  readonly totalElements = input.required<number>();
  readonly pageSize = input.required<number>();
  /** What is being counted, for the summary line: "40 orders". */
  readonly unit = input<string>();
  readonly label = input<string>();

  readonly pageChange = output<number>();

  protected readonly resolvedLabel = computed(
    () => this.label() ?? this.transloco.translate('shared.pagination.label'),
  );

  protected readonly summary = computed(() => {
    this.transloco.activeLang();
    const total = this.totalElements();
    const unit = this.unit() ?? this.transloco.translate('shared.pagination.records');
    if (total === 0) {
      return this.transloco.translate('shared.pagination.noRecords', {unit});
    }
    const first = this.page() * this.pageSize() + 1;
    const last = Math.min(total, first + this.pageSize() - 1);
    // Localised digits, so the line does not mix Latin numerals into an Arabic sentence.
    const digits = (value: number) => this.localeFormat.localizeNumber(value, 'decimal');
    return this.transloco.translate('shared.pagination.summary', {
      first: digits(first),
      last: digits(last),
      total: digits(total),
      unit,
    });
  });

  /** Page indices to render; `null` is an elision. */
  protected readonly slots = computed<readonly (number | null)[]>(() => {
    const total = this.totalPages();
    if (total <= WINDOW + 2) {
      return Array.from({length: total}, (_, index) => index);
    }

    const span = Math.floor(WINDOW / 2);
    // Clamp the window so it stays WINDOW wide even at the ends of the list.
    const start = Math.min(Math.max(this.page() - span, 1), total - WINDOW - 1);
    const end = start + WINDOW - 1;

    return [
      0,
      ...(start > 1 ? [null] : []),
      ...Array.from({length: end - start + 1}, (_, index) => start + index),
      ...(end < total - 2 ? [null] : []),
      total - 1,
    ];
  });

  protected go(page: number): void {
    if (page < 0 || page > this.totalPages() - 1 || page === this.page()) {
      return;
    }
    this.pageChange.emit(page);
  }
}
