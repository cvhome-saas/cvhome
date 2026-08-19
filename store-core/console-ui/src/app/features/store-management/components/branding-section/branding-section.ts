import {Component, input, linkedSignal, output} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {ImageBroken} from '@shared/directives/image-loaded';
import {FileDrop} from '@shared/ui/file-drop/file-drop';
import {Icon} from '@shared/ui/icon/icon';
import {Panel} from '@shared/ui/panel/panel';
import type {BrandingSettings} from '@models/store-settings';

/**
 * Logo and banner.
 *
 * Uploading is a multipart POST of its own, so a picked file is sent immediately rather than held
 * for *Save changes* — the section carries no form fields, and an image that appeared only after a
 * separate save would be a confusing thing to offer. Replacing is the same call: the server keeps
 * one logo and one banner, so a second upload overwrites the first.
 *
 * **Removing is not offered, because it cannot be done.** `MerchantStoreApi` maps `addLogo` and
 * `addBanner` and no delete counterpart, and `PersistableMerchantStore` carries neither image, so
 * an update cannot clear one either. seller-core has `removeStoreLogo`/`removeStoreBanner` methods
 * pointing at unmapped paths — they have always 404'd. A Remove button that cannot remove is worse
 * than none, so the card says what is possible instead.
 * TODO(lessons.md): see lessons.md, "Store management — a logo or banner can be uploaded but
 * never removed".
 */
@Component({
  selector: 'app-branding-section',
  imports: [FileDrop, ImageBroken, Icon, Panel, TranslocoDirective],
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
            (filesSelected)="pick('logo', $event)"
          >
            @if (!logoBroken() && branding().logo?.url; as url) {
              <img
                appImageBroken
                class="logo-preview"
                [src]="url"
                [alt]="t('storeSettings.branding.currentLogoAlt')"
                (broken)="logoBroken.set(true)"
              />
            } @else {
              <!-- No stored render, or a path this browser cannot reach: the store's initial stands in. -->
              <span class="logo-mark" aria-hidden="true">{{ initial() }}</span>
            }
          </app-file-drop>

          @if (branding().logo; as logo) {
            <p class="slot-current" dir="auto">
              {{ t('storeSettings.branding.currentFile', {name: logo.name}) }}
            </p>
          }
          @if (uploading() === 'logo') {
            <p class="slot-busy" role="status">{{ t('storeSettings.branding.uploadingLogo') }}</p>
          }
        </div>

        <div class="slot">
          <p class="field-label">{{ t('storeSettings.branding.banner') }}</p>

          <app-file-drop
            class="banner-drop"
            [label]="t('storeSettings.branding.dropBanner')"
            [hint]="t('storeSettings.branding.bannerHint')"
            accept="image/jpeg,image/png"
            (filesSelected)="pick('banner', $event)"
          >
            @if (!bannerBroken() && branding().banner?.url; as url) {
              <img
                appImageBroken
                class="banner-preview"
                [src]="url"
                [alt]="t('storeSettings.branding.currentBannerAlt')"
                (broken)="bannerBroken.set(true)"
              />
            } @else {
              <app-icon name="images" />
            }
          </app-file-drop>

          @if (branding().banner; as banner) {
            <p class="slot-current" dir="auto">
              {{ t('storeSettings.branding.currentFile', {name: banner.name}) }}
            </p>
          }
          @if (uploading() === 'banner') {
            <p class="slot-busy" role="status">{{ t('storeSettings.branding.uploadingBanner') }}</p>
          }
        </div>

        <p class="branding-note">{{ t('storeSettings.branding.replaceOnlyNote') }}</p>
      </div>
    </app-panel>
  `,
  styleUrls: ['../settings-card.css', './branding-section.css'],
})
export class BrandingSection {
  readonly branding = input.required<BrandingSettings>();
  readonly storeName = input.required<string>();
  /** Which upload is in flight, so the slot that is busy is the one that says so. */
  readonly uploading = input<'logo' | 'banner' | 'slide' | null>(null);

  readonly picked = output<{kind: 'logo' | 'banner'; file: File}>();

  /*
   * `ReadableImage.path` is where the pod put the file, which is not necessarily a URL this
   * browser can reach — the same gap the invoice hit in Module 4. Keyed off the URL so that
   * uploading a replacement clears the failure rather than inheriting it.
   */
  protected readonly logoBroken = linkedSignal<string | null, boolean>({
    source: () => this.branding().logo?.url ?? null,
    computation: () => false,
  });
  protected readonly bannerBroken = linkedSignal<string | null, boolean>({
    source: () => this.branding().banner?.url ?? null,
    computation: () => false,
  });

  protected initial(): string {
    return this.storeName().charAt(0).toUpperCase();
  }

  protected pick(kind: 'logo' | 'banner', files: readonly File[]): void {
    const file = files[0];
    if (file) {
      this.picked.emit({kind, file});
    }
  }
}
