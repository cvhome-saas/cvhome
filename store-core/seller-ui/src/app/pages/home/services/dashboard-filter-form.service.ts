import {Injectable, inject} from '@angular/core';
import {FormControl} from '@angular/forms';
import {DateRangeStateService} from '../state/date-range.state';

/**
 * Builds the two date-picker FormControls used by the dashboard filter bar.
 * The controls exist only to satisfy the Nebular datepicker's [formControl] API —
 * the actual date-range state lives in DateRangeStateService / the owning Facade.
 */
@Injectable()
export class DashboardFilterFormService {
  private readonly dateRangeState = inject(DateRangeStateService);

  readonly fromDateControl = new FormControl<Date>(this.dateRangeState.fromDate());
  readonly toDateControl = new FormControl<Date>(this.dateRangeState.toDate());
}
