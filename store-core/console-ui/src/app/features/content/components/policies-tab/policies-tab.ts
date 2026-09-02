import {Component, computed, inject, input, output} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {ContentCache} from '@api/content/content-cache';
import {PoliciesService} from '@api/content/policies.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import type {PolicyCompliance} from '@models/content';
import {Badge, Icon, Panel} from '@cvhome-saas/ui-kit/ui';
import type {BadgeTone, IconName} from '@cvhome-saas/ui-kit/ui';
import {STATUS_TONES} from '../content-list/content-list';

const ICONS: Readonly<Record<string, IconName>> = {
  TERMS: 'file',
  PRIVACY: 'lock',
  RETURNS: 'undo',
  SHIPPING: 'truck',
  COOKIES: 'alertCircle',
  CUSTOM: 'shield',
};

/**
 * "Legal & policies" compliance cards — one per policy type the platform knows, saying who requires it
 * and whether the store has one; a missing required type is the design's amber "Not written" row.
 * Sits above the ordinary list of policy heads.
 */
@Component({
  selector: 'app-policies-tab',
  imports: [Badge, Icon, Panel, TranslocoDirective],
  template: `
    <app-panel
      [title]="t('content.policies.title')"
      [subtitle]="t('content.policies.subtitle')"
      padded
      *transloco="let t"
    >
      <ul class="compliance">
        @for (row of rows(); track row.type) {
          <li
            [class.missing]="row.status === null && row.requiredBy.length"
            [class.clickable]="row.id !== null || canManage()"
          >
            <button
              class="row-button"
              type="button"
              (click)="pick(row)"
              [disabled]="row.id === null && !canManage()"
            >
              <span
                class="row-icon"
                [class.live]="row.status === 'PUBLISHED'"
                [class.warn]="row.status === null && row.requiredBy.length"
                ><app-icon [name]="icon(row)" [size]="14"
              /></span>
              <span class="row-copy">
                <strong>{{ t('content.policy.type.' + row.type + '.label') }}</strong>
                <small>{{ meta(row) }}</small>
              </span>
              @if (row.status; as status) {
                <app-badge [tone]="tone(status)" shape="square">{{
                  t('content.status.' + status)
                }}</app-badge>
              } @else if (row.requiredBy.length) {
                <app-badge tone="amber" shape="square">{{
                  t('content.policies.notWritten')
                }}</app-badge>
              } @else {
                <app-badge tone="slate" shape="square">{{
                  t('content.policies.optional')
                }}</app-badge>
              }
              <app-icon name="chevronRight" [size]="14" [flip]="true" />
            </button>
          </li>
        }
      </ul>
    </app-panel>
  `,
  styleUrl: './policies-tab.css',
})
export class PoliciesTab {
  private readonly api = inject(PoliciesService);
  private readonly transloco = inject(TranslocoService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly cache = inject(ContentCache);

  readonly canManage = input(true);
  /** Open the head (id) or start a new one of that type. */
  readonly open = output<PolicyCompliance>();

  private readonly resource = rxResource({
    params: () => {
      this.cache.stamp();
      return this.shell.currentStoreId() ?? undefined;
    },
    stream: () => this.api.compliance(),
  });

  protected readonly rows = computed<readonly PolicyCompliance[]>(() =>
    this.resource.hasValue() ? this.resource.value() : [],
  );

  protected icon(row: PolicyCompliance): IconName {
    return ICONS[row.type] ?? 'shield';
  }

  protected tone(status: string): BadgeTone {
    return STATUS_TONES[status as keyof typeof STATUS_TONES] ?? 'slate';
  }

  protected meta(row: PolicyCompliance): string {
    this.transloco.activeLang();
    if (row.requiredBy.length) {
      return this.transloco.translate('content.policies.requiredIn', {
        regions: row.requiredBy.join(', '),
      });
    }
    return this.transloco.translate('content.policies.notRequired');
  }

  protected pick(row: PolicyCompliance): void {
    this.open.emit(row);
  }
}
