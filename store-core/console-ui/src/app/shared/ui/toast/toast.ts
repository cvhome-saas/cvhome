import {Injectable, signal} from '@angular/core';

import {NotificationPort} from '@core/errors/notification.port';

export type ToastTone = 'success' | 'info' | 'warning' | 'danger';

export interface ToastMessage {
  readonly id: string;
  readonly tone: ToastTone;
  readonly text: string;
  /** How long until this toast auto-dismisses. `null` means it stays until closed. */
  readonly durationMs: number | null;
}

/** How long each tone stays up before auto-dismissing. Danger persists — an error is
 *  worth reading, not racing against a clock. */
const TONE_DURATION_MS: Record<ToastTone, number | null> = {
  success: 4500,
  info: 4500,
  warning: 6000,
  danger: null,
};

interface Timer {
  handle: ReturnType<typeof setTimeout>;
  /** Time left when last (re)started, so a pause/resume cycle doesn't reset the clock. */
  remainingMs: number;
  startedAt: number;
}

@Injectable({providedIn: 'root'})
export class ToastService implements NotificationPort {
  private readonly messagesState = signal<readonly ToastMessage[]>([]);
  readonly messages = this.messagesState.asReadonly();

  private readonly timers = new Map<string, Timer>();

  success(text: string): void {
    this.push('success', text);
  }

  info(text: string): void {
    this.push('info', text);
  }

  warning(text: string): void {
    this.push('warning', text);
  }

  danger(text: string): void {
    this.push('danger', text);
  }

  dismiss(id: string): void {
    this.clearTimer(id);
    this.messagesState.update((messages) => messages.filter((message) => message.id !== id));
  }

  /** Freezes the auto-dismiss countdown — called while the pointer is over the toast. */
  pause(id: string): void {
    const timer = this.timers.get(id);
    if (!timer) {
      return;
    }
    clearTimeout(timer.handle);
    timer.remainingMs -= Date.now() - timer.startedAt;
  }

  /** Restarts the countdown from wherever `pause` left it. */
  resume(id: string): void {
    const timer = this.timers.get(id);
    if (!timer) {
      return;
    }
    timer.startedAt = Date.now();
    timer.handle = setTimeout(() => this.dismiss(id), Math.max(0, timer.remainingMs));
  }

  private push(tone: ToastTone, text: string): void {
    const id = crypto.randomUUID();
    const durationMs = TONE_DURATION_MS[tone];
    this.messagesState.update((messages) => [...messages, {id, tone, text, durationMs}]);

    if (durationMs !== null) {
      this.timers.set(id, {
        handle: setTimeout(() => this.dismiss(id), durationMs),
        remainingMs: durationMs,
        startedAt: Date.now(),
      });
    }
  }

  private clearTimer(id: string): void {
    const timer = this.timers.get(id);
    if (timer) {
      clearTimeout(timer.handle);
      this.timers.delete(id);
    }
  }
}
