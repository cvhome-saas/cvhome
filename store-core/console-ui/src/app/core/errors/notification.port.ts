import {InjectionToken} from '@angular/core';

/**
 * Narrowed to what `ApiErrorService` actually calls. `NotificationService.success`/`info` take an
 * i18n *key*, not a message, so widening this port to match them would let a typed call reach one
 * implementer with a key and another with literal text.
 */
export interface NotificationPort {
  danger(message: string): void;
}

export const NOTIFICATION_PORT = new InjectionToken<NotificationPort>('NOTIFICATION_PORT');
