/**
 * The date arithmetic and locale formatting a calendar needs, with no component attached.
 *
 * Extracted when the single-date picker was built: it and `DateRangePicker` need the same twelve
 * functions, and the alternative was a second copy of `addDays` that could drift from the first.
 * Everything here is a pure function over local time — a calendar shows the reader's own days, so
 * `Date`'s local accessors are the right ones and UTC would shift the grid by a day for half the
 * world.
 */

/** Midnight local time on the same day. The comparison key for everything below. */
export function dateOnly(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate());
}

export function addDays(date: Date, days: number): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate() + days);
}

export function addMonths(date: Date, months: number): Date {
  return new Date(date.getFullYear(), date.getMonth() + months, 1);
}

export function startOfMonth(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), 1);
}

/**
 * `YYYY-MM-DD` in local time — the calendar's cell key, and the wire format for a Java `LocalDate`.
 *
 * Deliberately not `toISOString().slice(0, 10)`, which converts to UTC first: for a reader east of
 * Greenwich that returns the previous day, so a store created on the 1st would be sent as the 31st.
 */
export function dateKey(date: Date): string {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}

/** A `YYYY-MM-DD` back to a local `Date`, or null if it is not one. */
export function parseDateKey(value: string | null | undefined): Date | null {
  if (!value) {
    return null;
  }
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value.trim());
  if (!match) {
    return null;
  }
  const [, year, month, day] = match;
  const date = new Date(Number(year), Number(month) - 1, Number(day));
  // Rejects the 31st of February, which `Date` would otherwise roll forward into March.
  return dateKey(date) === value.trim() ? date : null;
}

export function sameDay(left: Date | null, right: Date | null): boolean {
  return !!left && !!right && dateKey(left) === dateKey(right);
}

export function isBefore(left: Date, right: Date): boolean {
  return dateOnly(left).getTime() < dateOnly(right).getTime();
}

export function isAfter(left: Date, right: Date): boolean {
  return dateOnly(left).getTime() > dateOnly(right).getTime();
}

export function isWithin(date: Date, start: Date | null, end: Date | null): boolean {
  if (!start || !end) {
    return false;
  }
  const time = dateOnly(date).getTime();
  return time >= dateOnly(start).getTime() && time <= dateOnly(end).getTime();
}

/** Whether a date falls outside an optional min/max window. */
export function outsideBounds(date: Date, min: Date | null, max: Date | null): boolean {
  return (!!min && isBefore(date, min)) || (!!max && isAfter(date, max));
}

/**
 * Every `Intl.DateTimeFormat` a calendar needs, built for one locale.
 *
 * A fresh set per active language rather than module-level constants: the originals were hardcoded
 * to `'en-US'`, which is exactly the kind of "translated everything except the dates" gap that is
 * easy to miss. `weekdays` is derived rather than listed, so it never has to be transcribed by hand
 * for a new language.
 */
export interface DateFormatters {
  readonly month: Intl.DateTimeFormat;
  readonly monthShort: Intl.DateTimeFormat;
  readonly day: Intl.DateTimeFormat;
  readonly fullDay: Intl.DateTimeFormat;
  readonly range: Intl.DateTimeFormat;
  readonly weekdays: readonly string[];
}

export function buildFormatters(locale: string): DateFormatters {
  const monday = new Date(2026, 0, 5);
  return {
    month: new Intl.DateTimeFormat(locale, {month: 'long', year: 'numeric'}),
    monthShort: new Intl.DateTimeFormat(locale, {month: 'short', year: 'numeric'}),
    day: new Intl.DateTimeFormat(locale, {day: 'numeric', month: 'short'}),
    fullDay: new Intl.DateTimeFormat(locale, {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    }),
    range: new Intl.DateTimeFormat(locale, {day: '2-digit', month: '2-digit', year: 'numeric'}),
    weekdays: weekdayNames(locale, monday),
  };
}

/**
 * Seven column headings that fit a seven-column grid.
 *
 * `short` where it fits — `Mon Tue Wed` is far more readable than `M T W T F S S`, which repeats
 * three letters. Arabic's `short` names are the *full* words (`الأربعاء`), which collide into each
 * other in a 2rem column and rendered as an unreadable smear, so those fall back to `narrow`.
 *
 * The rule is a width test rather than a list of locales, so a language nobody has tried yet gets
 * the right answer without anyone remembering to add it. Three characters is the cutoff because
 * that is what every abbreviating locale uses and what the column was sized for.
 *
 * Nothing is lost to assistive tech either way: each day cell carries the full weekday in its
 * `aria-label`, and this row is `aria-hidden`.
 */
function weekdayNames(locale: string, monday: Date): readonly string[] {
  const of = (style: 'short' | 'narrow') => {
    const formatter = new Intl.DateTimeFormat(locale, {weekday: style});
    return Array.from({length: 7}, (_, index) => formatter.format(addDays(monday, index)));
  };
  const short = of('short');
  return short.every((name) => name.length <= 3) ? short : of('narrow');
}

/**
 * The cells of one month, Monday-first, padded to whole weeks with nulls.
 *
 * Week-start is fixed to Monday rather than read from the locale: `Intl.Locale.prototype.weekInfo`
 * is not available in every browser the console targets, and a grid whose first column silently
 * changes meaning between browsers is worse than one that is consistently Monday. The weekday
 * *names* above are locale-correct, which is the part a reader actually reads.
 */
export function monthGrid(month: Date): readonly (Date | null)[] {
  const first = startOfMonth(month);
  const leading = (first.getDay() + 6) % 7;
  const daysInMonth = new Date(first.getFullYear(), first.getMonth() + 1, 0).getDate();

  const cells: (Date | null)[] = Array.from({length: leading}, () => null);
  for (let day = 1; day <= daysInMonth; day++) {
    cells.push(new Date(first.getFullYear(), first.getMonth(), day));
  }
  while (cells.length % 7 !== 0) {
    cells.push(null);
  }
  return cells;
}
