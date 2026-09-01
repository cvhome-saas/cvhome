import {inject} from '@angular/core';
import {CanDeactivateFn} from '@angular/router';
import {TranslocoService} from '@jsverse/transloco';

/**
 * A page opts into leave-confirmation by exposing `canLeave()`; returning false raises the browser
 * confirm. The prompt is a native `confirm` on purpose: a route guard runs outside the page's
 * template, where the shared dialog component has nothing to attach to.
 */
export interface ConfirmsLeave {
  canLeave(): boolean;
}

export const confirmLeave: CanDeactivateFn<ConfirmsLeave> = (component) => {
  if (component.canLeave()) {
    return true;
  }
  const transloco = inject(TranslocoService);
  return confirm(transloco.translate('shell.leave.unsaved'));
};
