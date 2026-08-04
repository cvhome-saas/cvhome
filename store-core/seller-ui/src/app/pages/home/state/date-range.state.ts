import {Injectable, signal} from '@angular/core';

/**
 * Generic, state-only holder for a from/to date range.
 * No API calls, no orchestration — each dashboard Facade owns when/why the range changes.
 */
@Injectable()
export class DateRangeStateService {
  private readonly _fromDate = signal<Date>(DateRangeStateService.daysAgo(7));
  private readonly _toDate = signal<Date>(new Date());

  readonly fromDate = this._fromDate.asReadonly();
  readonly toDate = this._toDate.asReadonly();

  setFromDate(date: Date): void {
    this._fromDate.set(date);
  }

  setToDate(date: Date): void {
    this._toDate.set(date);
  }

  private static daysAgo(days: number): Date {
    const date = new Date();
    date.setDate(date.getDate() - days);
    return date;
  }
}
