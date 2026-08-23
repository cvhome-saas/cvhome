import {Component, computed, inject, input, output} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import type {ContentStatus, TransitionAction} from '@models/content';
import {ActionMenu, type MenuAction} from '@shared/ui/action-menu/action-menu';
import {Badge} from '@shared/ui/badge/badge';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {ConfirmDialog} from '@shared/ui/confirm-dialog/confirm-dialog';
import {Icon} from '@shared/ui/icon/icon';
import {LoadError} from '@shared/ui/load-error/load-error';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {STATUS_TONES} from '../../components/content-list/content-list';

/** What the header's menu may offer; `schedule` opens the date prompt, `delete` the confirm. */
export type EditorCommand = TransitionAction | 'schedule' | 'delete';

const MENU_BY_STATUS: Readonly<Record<ContentStatus | 'NEW', readonly EditorCommand[]>> = {
  NEW: ['publish', 'schedule'],
  DRAFT: ['publish', 'schedule', 'submit-review', 'archive', 'delete'],
  REVIEW: ['publish', 'schedule', 'unpublish', 'archive', 'delete'],
  SCHEDULED: ['publish', 'unpublish', 'archive', 'delete'],
  PUBLISHED: ['unpublish', 'archive', 'delete'],
  ARCHIVED: ['restore', 'delete'],
};

/**
 * The chrome every content editor shares — the design's header (Cancel · Save draft · Publish with its
 * menu), the status plate, the two-column body (main cards, sidebar cards) and the busy/error states.
 */
@Component({
  selector: 'app-editor-shell',
  imports: [ActionMenu, Badge, BusyOverlay, ConfirmDialog, Icon, LoadError, PageHeader, TranslocoDirective],
  template: `
    <ng-container *transloco="let t">
      <app-page-header [title]="title()" [context]="context()">
        @if (status(); as s) {
          <app-badge [tone]="tone(s)" shape="square">{{ t('content.status.' + s) }}</app-badge>
        }
        <button class="secondary-action" type="button" (click)="cancelled.emit()">{{ t('content.editor.back') }}</button>
        @if (canManage()) {
          <button class="secondary-action" type="button" [disabled]="!canSave()" (click)="saved.emit()">
            <app-icon name="check" />
            {{ saving() ? t('content.editor.saving') : t('content.editor.saveDraft') }}
          </button>
          @if (primary(); as action) {
            <button class="primary-action" type="button" [disabled]="saving() || invalid()" (click)="commanded.emit(action.key)">
              <app-icon name="checkCircle" />
              {{ action.label }}
            </button>
          }
          @if (more().length) {
            <app-action-menu [actions]="more()" [ariaLabel]="t('content.editor.moreActions')" [disabled]="saving()" (picked)="commanded.emit($event.key)" />
          }
        }
      </app-page-header>

      @if (loadError(); as failure) {
        <app-load-error [message]="failure.message" (retry)="retried.emit()" />
      }

      <app-busy-overlay reserve="page" [busy]="loading()" [label]="t('content.editor.loading')">
        <div class="editor-layout">
          <div class="editor-main"><ng-content select="[main]" /></div>
          <aside class="editor-side"><ng-content select="[sidebar]" /></aside>
        </div>
      </app-busy-overlay>

      <app-confirm-dialog
        [open]="deleteOpen()"
        [title]="t('content.delete.title', {title: title()})"
        [message]="t('content.delete.message')"
        [confirmLabel]="t('content.action.delete')"
        [cancelLabel]="t('shared.actions.cancel')"
        (confirmed)="deleted.emit()"
        (dismissed)="deleteDismissed.emit()"
      />
    </ng-container>
  `,
  styleUrl: './editor-shell.css',
})
export class EditorShell {
  private readonly transloco = inject(TranslocoService);

  readonly title = input.required<string>();
  readonly context = input<string | null>(null);
  readonly status = input<ContentStatus | null>(null);
  readonly isNew = input(false);
  readonly loading = input(false);
  readonly saving = input(false);
  readonly canSave = input(false);
  readonly invalid = input(false);
  readonly canManage = input(true);
  readonly loadError = input<Error | null>(null);
  readonly deleteOpen = input(false);

  readonly saved = output<void>();
  readonly cancelled = output<void>();
  readonly retried = output<void>();
  readonly commanded = output<string>();
  readonly deleted = output<void>();
  readonly deleteDismissed = output<void>();

  private readonly commands = computed<readonly EditorCommand[]>(() => MENU_BY_STATUS[this.isNew() ? 'NEW' : (this.status() ?? 'DRAFT')]);

  /** The filled button: Publish while unpublished, Unpublish once live, Restore when archived. */
  protected readonly primary = computed<MenuAction | null>(() => {
    this.transloco.activeLang();
    const first = this.commands()[0];
    return first ? {key: first, label: this.transloco.translate(`content.action.${first}`)} : null;
  });

  protected readonly more = computed<readonly MenuAction[]>(() => {
    this.transloco.activeLang();
    return this.commands().slice(1).map((key) => ({
      key,
      label: this.transloco.translate(`content.action.${key}`),
      danger: key === 'delete',
    }));
  });

  protected tone(status: ContentStatus) {
    return STATUS_TONES[status];
  }
}
