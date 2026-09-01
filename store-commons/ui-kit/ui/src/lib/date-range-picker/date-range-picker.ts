import {
  Component,
  ElementRef,
  Injector,
  afterNextRender,
  computed,
  inject,
  input,
  model,
  output,
  signal,
  viewChild,
} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {
  addDays,
  addMonths,
  buildFormatters,
  dateKey,
  dateOnly,
  isBefore,
  isWithin,
  monthGrid,
  outsideBounds,
  sameDay,
  startOfMonth,
  type DateFormatters,
} from '@cvhome-saas/ui-kit/i18n';
import {Icon} from '../icon/icon';

export interface DateRangeValue {
  readonly from: Date | null;
  readonly to: Date | null;
}

interface CalendarDay {
  readonly date: Date | null;
  readonly key: string;
  readonly label: string;
  readonly fullLabel: string;
  readonly disabled: boolean;
  readonly today: boolean;
  /** An endpoint of the range — drawn as a filled pill. */
  readonly selected: boolean;
  /** Inside the range bar, endpoints included. Drives the continuous highlight. */
  readonly inBar: boolean;
  readonly barStart: boolean;
  readonly barEnd: boolean;
  readonly focused: boolean;
}

interface CalendarMonth {
  readonly key: string;
  readonly label: string;
  readonly days: readonly CalendarDay[];
}

/** How long the completed range stays visible before the panel closes itself. */
const COMMIT_DWELL_MS = 160;

@Component({
  selector: 'app-date-range-picker',
  imports: [Icon, TranslocoDirective],
  templateUrl: './date-range-picker.html',
  styleUrl: './date-range-picker.css',
  host: {
    '(document:click)': 'closeFromDocument($event)',
    '(document:keydown.escape)': 'cancel()',
  },
})
export class DateRangePicker {
  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly injector = inject(Injector);
  private readonly trigger = viewChild<ElementRef<HTMLButtonElement>>('trigger');
  private readonly transloco = inject(TranslocoService);
  private readonly localeService = inject(TranslocoLocaleService);

  readonly value = model<DateRangeValue>({from: null, to: null});
  readonly min = input<Date | null>(null);
  readonly max = input<Date | null>(null);
  readonly placeholder = input<string>();
  readonly disabled = input(false);
  readonly closed = output<DateRangeValue>();

  protected readonly open = signal(false);
  protected readonly draftFrom = signal<Date | null>(null);
  protected readonly draftTo = signal<Date | null>(null);
  protected readonly hoverDate = signal<Date | null>(null);
  protected readonly focusedDate = signal<Date | null>(null);
  protected readonly viewMonth = signal(startOfMonth(new Date()));
  protected readonly announcement = signal('');

  private readonly formatters = computed<DateFormatters>(() => {
    this.transloco.activeLang();
    return buildFormatters(this.localeService.getLocale());
  });

  protected readonly weekdays = computed(() => this.formatters().weekdays);

  protected readonly label = computed(() => {
    this.transloco.activeLang();
    const range = this.value();
    const {range: formatter} = this.formatters();
    const placeholder = this.placeholder() ?? this.transloco.translate('shared.dateRangePicker.selectRange');
    if (range.from && range.to) {
      return this.transloco.translate('shared.dateRangePicker.rangeLabel', {
        from: formatter.format(range.from),
        to: formatter.format(range.to),
      });
    }
    if (range.from) {
      return this.transloco.translate('shared.dateRangePicker.rangeLabelOpen', {
        from: formatter.format(range.from),
      });
    }
    return placeholder;
  });

  protected readonly months = computed(() => [
    this.buildMonth(this.viewMonth()),
    this.buildMonth(addMonths(this.viewMonth(), 1)),
  ]);

  private commitTimer: ReturnType<typeof setTimeout> | null = null;

  protected formatDay(date: Date): string {
    return this.formatters().day.format(date);
  }

  protected openPicker(): void {
    if (this.disabled()) {
      return;
    }
    const current = this.value();
    const focus = current.from ?? new Date();
    this.draftFrom.set(current.from ? dateOnly(current.from) : null);
    this.draftTo.set(current.to ? dateOnly(current.to) : null);
    this.focusedDate.set(dateOnly(focus));
    this.viewMonth.set(startOfMonth(focus));
    this.open.set(true);
    this.focusDayAfterRender(this.focusedDate());
  }

  /**
   * Dismisses without committing. There is no Cancel button — Escape, an outside click,
   * and re-clicking the trigger are the ways out of a half-finished selection.
   */
  protected cancel(): void {
    this.dismiss();
  }

  protected closeFromDocument(event: Event): void {
    if (event.target instanceof Node && !this.element.nativeElement.contains(event.target)) {
      this.dismiss();
    }
  }

  protected previousMonth(): void {
    this.viewMonth.update((month) => addMonths(month, -1));
  }

  protected nextMonth(): void {
    this.viewMonth.update((month) => addMonths(month, 1));
  }

  /**
   * Two clicks complete a range and it applies immediately — no confirmation step.
   *
   * Clicking a date earlier than the pending start restarts the selection from there
   * rather than swapping the endpoints: because the second click commits, a silent swap
   * would apply a range the user never deliberately chose.
   */
  protected selectDate(day: CalendarDay): void {
    if (!day.date || day.disabled) {
      return;
    }
    const selected = dateOnly(day.date);
    const from = this.draftFrom();
    const to = this.draftTo();
    this.focusedDate.set(selected);

    if (!from || to || isBefore(selected, from)) {
      this.draftFrom.set(selected);
      this.draftTo.set(null);
      return;
    }

    this.draftTo.set(selected);
    this.commit(from, selected);
  }

  protected onDayKeydown(event: KeyboardEvent, day: CalendarDay): void {
    if (!day.date) {
      return;
    }
    if (event.key === 'Enter' || event.key === ' ') {
      event.preventDefault();
      this.selectDate(day);
      return;
    }

    const movements: Record<string, number> = {
      ArrowLeft: -1,
      ArrowRight: 1,
      ArrowUp: -7,
      ArrowDown: 7,
      PageUp: 0,
      PageDown: 0,
    };
    if (!(event.key in movements)) {
      return;
    }
    event.preventDefault();

    let next: Date;
    if (event.key === 'PageUp') {
      next = addMonths(day.date, -1);
    } else if (event.key === 'PageDown') {
      next = addMonths(day.date, 1);
    } else {
      next = addDays(day.date, movements[event.key]);
    }

    this.focusedDate.set(next);
    if (isBefore(next, this.viewMonth())) {
      this.viewMonth.set(startOfMonth(next));
    }
    if (!isBefore(next, addMonths(this.viewMonth(), 2))) {
      this.viewMonth.set(startOfMonth(addMonths(next, -1)));
    }
    this.focusDayAfterRender(next);
  }

  private commit(from: Date, to: Date): void {
    this.value.set({from, to});
    const {fullDay} = this.formatters();
    this.announcement.set(
      this.transloco.translate('shared.dateRangePicker.rangeApplied', {
        from: fullDay.format(from),
        to: fullDay.format(to),
      }),
    );
    // Hold briefly so the completed range is visible before the panel animates away.
    this.commitTimer = setTimeout(() => {
      this.commitTimer = null;
      this.teardown();
      this.closed.emit(this.value());
    }, COMMIT_DWELL_MS);
  }

  private dismiss(): void {
    if (!this.open()) {
      return;
    }
    if (this.commitTimer !== null) {
      clearTimeout(this.commitTimer);
      this.commitTimer = null;
    }
    this.teardown();
    this.closed.emit(this.value());
  }

  private teardown(): void {
    this.open.set(false);
    this.hoverDate.set(null);
    this.trigger()?.nativeElement.focus();
  }

  /**
   * Moves focus to a day cell once it exists in the DOM.
   *
   * `afterNextRender` rather than a timer: the app runs with `eventCoalescing`, which can
   * defer change detection past a `setTimeout(0)`, so a timer would reach for a cell the
   * panel had not rendered yet.
   */
  private focusDayAfterRender(date: Date | null): void {
    if (!date) {
      return;
    }
    const key = dateKey(date);
    afterNextRender(
      () => {
        this.element.nativeElement
          .querySelector<HTMLButtonElement>(`[data-date="${key}"]`)
          ?.focus();
      },
      {injector: this.injector},
    );
  }

  private buildMonth(month: Date): CalendarMonth {
    const first = startOfMonth(month);
    const days = monthGrid(first).map((date, index) =>
      date ? this.calendarDay(date) : this.emptyDay(`${dateKey(first)}-empty-${index}`),
    );
    return {key: dateKey(first), label: this.formatters().month.format(first), days};
  }

  private emptyDay(key: string): CalendarDay {
    return {
      date: null,
      key,
      label: '',
      fullLabel: '',
      disabled: true,
      today: false,
      selected: false,
      inBar: false,
      barStart: false,
      barEnd: false,
      focused: false,
    };
  }

  private calendarDay(date: Date): CalendarDay {
    const from = this.draftFrom();
    const to = this.draftTo();
    const hover = this.hoverDate();

    // While only the start is chosen, the hovered day previews the end of the bar.
    const provisionalEnd = to ?? (from && hover && !isBefore(hover, from) ? hover : null);
    const barStartDate = from;
    const barEndDate = provisionalEnd;

    const min = this.min();
    const max = this.max();

    return {
      date,
      key: dateKey(date),
      label: String(date.getDate()),
      fullLabel: this.formatters().fullDay.format(date),
      disabled: outsideBounds(date, min, max),
      today: sameDay(date, new Date()),
      selected: sameDay(date, from) || sameDay(date, to),
      inBar: !!barEndDate && isWithin(date, barStartDate, barEndDate),
      barStart: !!barEndDate && sameDay(date, barStartDate),
      barEnd: !!barEndDate && sameDay(date, barEndDate),
      focused: sameDay(date, this.focusedDate()),
    };
  }
}
