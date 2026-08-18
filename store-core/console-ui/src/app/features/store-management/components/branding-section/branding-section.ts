import {Component, inject, input} from '@angular/core';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {FileDrop} from '@shared/ui/file-drop/file-drop';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import {ProgressTrack} from '@shared/ui/progress-track/progress-track';
import {ToastService} from '@shared/ui/toast/toast';
import type {BrandingSettings} from '@models/store-settings';

/**
 * Logo and banner.
 *
 * Both are uploads, and uploads have no endpoint yet, so every action here says so rather
 * than failing silently. The section carries no fields, which is why *Save changes* stays
 * disabled while it is open — there is nothing on this card to save.
 */
@Component({
  selector: 'app-branding-section',
  imports: [FileDrop, Icon, Panel, ProgressTrack, TranslocoDirective],
  template: `
    <app-panel
      [title]="t('storeSettings.branding.title')"
      [subtitle]="t('storeSettings.branding.subtitle')"
      *transloco="let t"
    >
      <div class="section-body branding">
        <div class="slot">
          <p class="field-label">{{ t('storeSettings.branding.logo') }}</p>

          <app-file-drop
            [label]="t('storeSettings.branding.dropOrBrowse')"
            [hint]="t('storeSettings.branding.logoHint')"
            accept="image/png,image/svg+xml"
            (filesSelected)="notSupported(t('storeSettings.branding.uploadingLogo'))"
          >
            @if (branding().logo?.url; as url) {
              <img class="logo-preview" [src]="url" [alt]="t('storeSettings.branding.currentLogoAlt')" />
            } @else {
              <!-- No stored render yet, so the tile stands in with the store's initial. -->
              <span class="logo-mark" aria-hidden="true">{{ initial() }}</span>
            }
          </app-file-drop>

          <div class="slot-actions">
            <button class="ghost-action" type="button" (click)="notSupported(t('storeSettings.branding.replacingLogo'))">
              {{ t('storeSettings.branding.replace') }}
            </button>
            <button class="danger-action" type="button" (click)="notSupported(t('storeSettings.branding.removingLogo'))">
              {{ t('storeSettings.branding.remove') }}
            </button>
          </div>
        </div>

        <div class="slot">
          <p class="field-label">{{ t('storeSettings.branding.banner') }}</p>

          <app-file-drop
            class="banner-drop"
            [label]="t('storeSettings.branding.dropBanner')"
            [hint]="t('storeSettings.branding.bannerHint')"
            accept="image/jpeg,image/png"
            (filesSelected)="notSupported(t('storeSettings.branding.uploadingBanner'))"
          >
            <app-icon name="images" />
          </app-file-drop>

          @if (branding().banner; as banner) {
            <div class="upload-row">
              <app-icon name="file" />
              <div class="upload-copy">
                <strong>{{ banner.name }}</strong>
                <app-progress-track [value]="branding().bannerProgress" />
              </div>
              <span class="upload-state">{{ stateOf(t) }}</span>
              <button
                class="icon-action"
                type="button"
                [attr.aria-label]="t('storeSettings.branding.removeNamed', {name: banner.name})"
                (click)="notSupported(t('storeSettings.branding.removingBanner'))"
              >
                <app-icon name="x" />
              </button>
            </div>
          }
        </div>
      </div>
    </app-panel>
  `,
  styleUrls: ['../settings-card.css', './branding-section.css'],
})
export class BrandingSection {
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly branding = input.required<BrandingSettings>();
  readonly storeName = input.required<string>();

  protected initial(): string {
    return this.storeName().charAt(0).toUpperCase();
  }

  protected stateOf(t: (key: string) => string): string {
    const progress = this.branding().bannerProgress;
    return progress >= 100 ? t('storeSettings.branding.uploaded') : `${progress}%`;
  }

  protected notSupported(what: string): void {
    this.toast.info(this.transloco.translate('storeSettings.notAvailable', {what}));
  }
}
