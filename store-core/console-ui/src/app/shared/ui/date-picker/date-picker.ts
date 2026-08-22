import {
  Component,
  ElementRef,
  Injector,
  afterNextRender,
  computed,
  forwardRef,
  inject,
  input,
  model,
  signal,
  viewChild,
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';
import {TranslocoLocaleService} from '@jsverse/transloco-locale';

import {
  addDays,
  addMonths,
  buildFormatters,
  dateKey,
  dateOnly,
  isBefore,
  monthGrid,
  outsideBounds,
  parseDateKey,
  sameDay,
  startOfMonth,
  type DateFormatters,
} from '@core/i18n/calendar';
import {Icon} from '@shared/ui/icon/icon';

interface CalendarDay {
  readonly date: Date | null;
  readonly key: string;
  readonly label: string;
  readonly fullLabel: string;
  readonly disabled: boolean;
  readonly today: boolean;
  readonly selected: boolean;
  readonly focused: boolean;
}

/** How long the chosen day stays visible before the panel closes itself. */
const COMMIT_DWELL_MS = 140;

/*
 * What the panel needs, from its own layout: 0.85rem padding twice, a 1.9rem header with a 0.6rem
 * margin, the weekday row, six 2.1rem week rows, and the footer. Constants rather than a
 * measurement because placement is decided *before* the panel renders — measuring the real element
 * would mean drawing it in the wrong place first and correcting it a frame later, which is a visible
 * jump. They only have to be close: the comparison is "does it fit", not "by how much".
 */
const PANEL_BLOCK_SIZE_PX = 360;
const PANEL_INLINE_SIZE_PX = 304;

/** Breathing room kept between the panel and the edge of the window. */
const VIEWPORT_MARGIN_PX = 8;

/**
 * A single calendar date, as a `YYYY-MM-DD` string.
 *
 * **Why this exists rather than `<input type="date">`.** The native control was what both store
 * forms used, and it is the one field on either page that the console does not actually control:
 * every browser renders its own trigger, its own calendar and its own placeholder, none of which
 * take the console's tokens, its font, its border radius or its dark theme. In a row of five
 * selects it read as a control borrowed from somewhere else — and in Arabic it kept the browser
 * UI's language and digits regardless of what the console was set to, because the native picker
 * follows the *browser* locale, not the app's. There is no styling hook that fixes either: the
 * shadow parts are non-standard and `::-webkit-calendar-picker-indicator` is the only one Chrome
 * exposes at all.
 *
 * So this is the console's own: the same popover, tokens, motion, focus ring and RTL handling as
 * `DateRangePicker`, sharing its calendar maths through `@core/i18n/calendar`, and formatted in
 * the console's active language.
 *
 * A `ControlValueAccessor`, so it drops into a reactive form exactly where the native input was:
 *
 * ```html
 * <app-date-picker id="store-since" formControlName="inBusinessSince" [max]="today" />
 * ```
 *
 * The value is `YYYY-MM-DD` — a Java `LocalDate` on the wire — and empty string means "not set",
 * which is what both `inBusinessSince` controls hold when the merchant has not answered.
 */
@Component({
  selector: 'app-date-picker',
  imports: [Icon, TranslocoDirective],
  templateUrl: './date-picker.html',
  styleUrl: './date-picker.css',
  host: {
    /*
     * The id belongs to the control this draws, not to the host.
     *
     * A static or bound `id` on a component element lands in the DOM *as well as* matching the
     * `id` input, so the host and the inner control ended up carrying the same id — invalid HTML,
     * and `<label for>` then resolves to the host, which is not a labelable element, so the
     * association silently does not happen. Found by probing where the id actually went; four of
     * these six components had shipped with it.
     */
    '[attr.id]': 'null',
    '(document:click)': 'closeFromDocument($event)',
    '(document:keydown.escape)': 'cancel()',
    '(window:resize)': 'reposition()',
  },
  providers: [
    {provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => DatePicker), multi: true},
  ],
})
export class DatePicker implements ControlValueAccessor {
  private readonly element = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly injector = inject(Injector);
  private readonly trigger = viewChild<ElementRef<HTMLButtonElement>>('trigger');
  private readonly transloco = inject(TranslocoService);
  private readonly localeService = inject(TranslocoLocaleService);

  /** `YYYY-MM-DD`, or `''` for no date. Two-way bindable when used without a form. */
  readonly value = model<string>('');
  /** `YYYY-MM-DD` bounds. A day outside them is rendered but not selectable. */
  readonly min = input<string>('');
  readonly max = input<string>('');
  readonly placeholder = input<string | null>(null);
  /** Mirrors the native `id`, so an existing `<label for>` keeps working. */
  readonly id = input<string | null>(null);
  /** Names the trigger for assistive tech where no visible label points at it. */
  readonly ariaLabel = input<string | null>(null);
  readonly disabled = input(false);
  /** Whether the field may be emptied again. False where the form requires an answer. */
  readonly clearable = input(true);

  protected readonly open = signal(false);

  /**
   * Which way the panel opens.
   *
   * Always-downward is wrong for a field low on a long form: the panel runs off the bottom, and the
   * browser scrolls the page to chase the focused day cell — so opening the picker yanked the form
   * out from under the reader. Decided per open from the room actually available, on both axes.
   */
  protected readonly dropUp = signal(false);
  protected readonly alignEnd = signal(false);
  protected readonly focusedDate = signal<Date | null>(null);
  protected readonly viewMonth = signal(startOfMonth(new Date()));
  protected readonly announcement = signal('');

  /** Set by `setDisabledState`; ORed with the `disabled` input so either source wins. */
  private readonly formDisabled = signal(false);
  protected readonly isDisabled = computed(() => this.disabled() || this.formDisabled());

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;
  private commitTimer: ReturnType<typeof setTimeout> | null = null;

  private readonly formatters = computed<DateFormatters>(() => {
    this.transloco.activeLang();
    return buildFormatters(this.localeService.getLocale());
  });

  protected readonly weekdays = computed(() => this.formatters().weekdays);
  protected readonly selectedDate = computed(() => parseDateKey(this.value()));
  protected readonly minDate = computed(() => parseDateKey(this.min()));
  protected readonly maxDate = computed(() => parseDateKey(this.max()));

  protected readonly label = computed(() => {
    this.transloco.activeLang();
    const selected = this.selectedDate();
    if (selected) {
      return this.formatters().range.format(selected);
    }
    return this.placeholder() ?? this.transloco.translate('shared.datePicker.selectDate');
  });

  protected readonly hasValue = computed(() => this.selectedDate() !== null);

  protected readonly monthLabel = computed(() => {
    this.transloco.activeLang();
    return this.formatters().month.format(this.viewMonth());
  });

  protected readonly days = computed<readonly CalendarDay[]>(() =>
    monthGrid(this.viewMonth()).map((date, index) => this.toDay(date, index)),
  );

  /** Whether "Today" would land on a day the bounds allow. */
  protected readonly todaySelectable = computed(
    () => !outsideBounds(new Date(), this.minDate(), this.maxDate()),
  );

  writeValue(value: string | null): void {
    this.value.set(value ?? '');
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.formDisabled.set(isDisabled);
  }

  protected toggle(): void {
    if (this.open()) {
      this.cancel();
    } else {
      this.openPicker();
    }
  }

  protected openPicker(): void {
    if (this.isDisabled()) {
      return;
    }
    const focus = this.selectedDate() ?? this.clampToBounds(new Date());
    this.focusedDate.set(focus);
    this.viewMonth.set(startOfMonth(focus));
    this.place();
    this.open.set(true);
    this.focusDayAfterRender(focus);
  }

  /** Dismisses without choosing. Escape, an outside click and re-clicking the trigger all land here. */
  protected cancel(): void {
    if (!this.open()) {
      return;
    }
    if (this.commitTimer !== null) {
      clearTimeout(this.commitTimer);
      this.commitTimer = null;
    }
    this.teardown();
  }

  /**
   * Re-decides placement while the panel is open.
   *
   * A resize can turn a fit into an overflow, and the panel is anchored to the trigger rather than
   * to the viewport, so nothing else would notice.
   */
  protected reposition(): void {
    if (this.open()) {
      this.place();
    }
  }

  protected closeFromDocument(event: Event): void {
    if (event.target instanceof Node && !this.element.nativeElement.contains(event.target)) {
      this.cancel();
    }
  }

  protected previousMonth(): void {
    this.viewMonth.update((month) => addMonths(month, -1));
  }

  protected nextMonth(): void {
    this.viewMonth.update((month) => addMonths(month, 1));
  }

  protected selectDate(day: CalendarDay): void {
    if (!day.date || day.disabled) {
      return;
    }
    this.focusedDate.set(day.date);
    this.commit(day.date);
  }

  protected selectToday(): void {
    if (!this.todaySelectable()) {
      return;
    }
    this.commit(dateOnly(new Date()));
  }

  /**
   * Empties the field.
   *
   * Offered because both fields that use this picker are optional, and a date control with no way
   * back to "not set" quietly turns an optional field into a required one the moment it is touched.
   */
  protected clear(): void {
    this.publish('');
    this.announcement.set(this.transloco.translate('shared.datePicker.cleared'));
    this.teardown();
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

    const steps: Record<string, number> = {ArrowLeft: -1, ArrowRight: 1, ArrowUp: -7, ArrowDown: 7};
    let next: Date | null = null;

    if (event.key in steps) {
      next = addDays(day.date, steps[event.key]);
    } else if (event.key === 'PageUp') {
      next = addMonths(day.date, -1);
    } else if (event.key === 'PageDown') {
      next = addMonths(day.date, 1);
    } else if (event.key === 'Home') {
      next = startOfMonth(day.date);
    } else if (event.key === 'End') {
      next = new Date(day.date.getFullYear(), day.date.getMonth() + 1, 0);
    }

    if (!next) {
      return;
    }
    event.preventDefault();
    this.focusedDate.set(next);
    // One month is shown, so any move off it is a page turn rather than a scroll.
    if (next.getMonth() !== this.viewMonth().getMonth() || next.getFullYear() !== this.viewMonth().getFullYear()) {
      this.viewMonth.set(startOfMonth(next));
    }
    this.focusDayAfterRender(next);
  }

  private commit(date: Date): void {
    this.publish(dateKey(date));
    this.announcement.set(
      this.transloco.translate('shared.datePicker.applied', {date: this.formatters().fullDay.format(date)}),
    );
    // Hold briefly so the chosen day is visible before the panel animates away.
    this.commitTimer = setTimeout(() => {
      this.commitTimer = null;
      this.teardown();
    }, COMMIT_DWELL_MS);
  }

  private publish(value: string): void {
    this.value.set(value);
    this.onChange(value);
    this.onTouched();
  }

  private teardown(): void {
    this.open.set(false);
    this.onTouched();
    this.trigger()?.nativeElement.focus();
  }

  /**
   * Picks the corner of the trigger the panel hangs from.
   *
   * Downward and start-aligned unless that would overflow the window *and* the other side has more
   * room — a panel that flips on a near-miss is more disorienting than one that is slightly tight.
   * The inline axis is measured in the writing direction, so this is correct in Arabic without a
   * second branch: in RTL the field's start edge is its right one, and the room that matters is what
   * lies to its left.
   */
  private place(): void {
    const element = this.trigger()?.nativeElement;
    if (!element || typeof window === 'undefined') {
      return;
    }
    const rect = element.getBoundingClientRect();

    const below = window.innerHeight - rect.bottom;
    const above = rect.top;
    this.dropUp.set(below < PANEL_BLOCK_SIZE_PX + VIEWPORT_MARGIN_PX && above > below);

    const rtl = getComputedStyle(element).direction === 'rtl';
    const roomFromStart = rtl ? rect.right : window.innerWidth - rect.left;
    this.alignEnd.set(roomFromStart < PANEL_INLINE_SIZE_PX + VIEWPORT_MARGIN_PX);
  }

  /** The nearest day the bounds allow, for opening on a sensible month when nothing is chosen. */
  private clampToBounds(date: Date): Date {
    const min = this.minDate();
    const max = this.maxDate();
    if (min && isBefore(date, min)) {
      return min;
    }
    if (max && isBefore(max, date)) {
      return max;
    }
    return dateOnly(date);
  }

  /**
   * Moves focus to a day cell once it exists in the DOM.
   *
   * `afterNextRender` rather than a timer: the app runs with `eventCoalescing`, which can defer
   * change detection past a `setTimeout(0)`, so a timer would reach for a cell the panel had not
   * rendered yet.
   */
  private focusDayAfterRender(date: Date | null): void {
    if (!date) {
      return;
    }
    const key = dateKey(date);
    afterNextRender(
      () => {
        this.element.nativeElement.querySelector<HTMLButtonElement>(`[data-date="${key}"]`)?.focus();
      },
      {injector: this.injector},
    );
  }

  private toDay(date: Date | null, index: number): CalendarDay {
    if (!date) {
      return {
        date: null,
        key: `${dateKey(this.viewMonth())}-empty-${index}`,
        label: '',
        fullLabel: '',
        disabled: true,
        today: false,
        selected: false,
        focused: false,
      };
    }
    return {
      date,
      key: dateKey(date),
      label: String(date.getDate()),
      fullLabel: this.formatters().fullDay.format(date),
      disabled: outsideBounds(date, this.minDate(), this.maxDate()),
      today: sameDay(date, new Date()),
      selected: sameDay(date, this.selectedDate()),
      focused: sameDay(date, this.focusedDate()),
    };
  }
}
