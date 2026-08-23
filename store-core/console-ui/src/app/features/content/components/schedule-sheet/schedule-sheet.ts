import {Component, input, model, output} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {DateTimeField} from '@shared/ui/date-time-field/date-time-field';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';

/** The "Schedule…" prompt every editor shows: a date-time and a confirm. */
@Component({
  selector: 'app-schedule-sheet',
  imports: [DateTimeField, Icon, Panel, TranslocoDirective],
  template: `
    @if (open()) {
      <div class="schedule-sheet" role="dialog" [attr.aria-label]="t('content.editor.scheduleTitle')" *transloco="let t">
        <app-panel [title]="t('content.editor.scheduleTitle')" [subtitle]="t('content.editor.scheduleSubtitle')" padded>
          <div class="schedule-row">
            <app-date-time-field [value]="at()" (valueChange)="at.set($event)" [ariaLabel]="t('content.editor.publishAt')" />
            <button class="primary-action" type="button" [disabled]="!at()" (click)="confirmed.emit(at()); open.set(false)">
              <app-icon name="calendar" />{{ t('content.action.schedule') }}
            </button>
            <button class="ghost-action" type="button" (click)="open.set(false)">{{ t('shared.actions.cancel') }}</button>
          </div>
        </app-panel>
      </div>
    }
  `,
  styles: `
    .schedule-sheet { position: fixed; inset-block-end: 1.25rem; inset-inline-end: 1.25rem; z-index: 30;
      inline-size: min(30rem, calc(100vw - 2.5rem)); box-shadow: var(--lift); }
    .schedule-row { display: flex; flex-wrap: wrap; align-items: center; gap: 0.5rem; }
    .schedule-row app-date-time-field { flex: 1 1 12rem; }
  `,
})
export class ScheduleSheet {
  readonly open = model(false);
  readonly at = model('');
  readonly confirmed = output<string>();
  readonly minAt = input('');
}
