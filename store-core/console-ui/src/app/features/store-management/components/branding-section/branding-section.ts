import {Component, computed, input, output} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {ImagePicker, type ImageRules} from '@shared/ui/image-picker/image-picker';
import {Panel} from '@shared/ui/panel/panel';
import type {BrandingSettings} from '@models/store-settings';

/**
 * The store's logo and storefront banner.
 *
 * Each asset is a framed well drawn at its own proportions — square for the logo, 4:1 for the
 * banner — so the two are legible as different kinds of thing before a word is read, and so a
 * wrongly-shaped upload cannot look correct in a box that does not hold a shape. The frame is the
 * control: click it, drop on it, or tab to it.
 *
 * Uploading is immediate rather than held for *Save changes*, because each is a multipart POST of
 * its own and this section's form has no controls to submit. What the well refuses never becomes a
 * request at all: `ImagePicker` checks type, weight and pixel dimensions first.
 *
 * There is no Remove. `MerchantStoreApi` has `addLogo` and `addBanner` and no delete counterpart,
 * and `PersistableMerchantStore` carries neither image, so clearing one is not expressible — the
 * note under the wells says so. TODO(lessons.md): see lessons.md, "Store management — a logo or
 * banner can be uploaded but never removed".
 */
@Component({
  selector: 'app-branding-section',
  imports: [ImagePicker, Panel, TranslocoDirective],
  template: `
    <app-panel
      [title]="t('storeSettings.branding.title')"
      [subtitle]="t('storeSettings.branding.subtitle')"
      *transloco="let t"
    >
      <div class="section-body branding">
        <div class="slot">
          <p class="field-label">{{ t('storeSettings.branding.logo') }}</p>
          <app-image-picker
            icon="palette"
            [label]="t('storeSettings.branding.dropOrBrowse')"
            [rules]="logoRules"
            [url]="branding().logo?.url ?? null"
            [fileName]="branding().logo?.name ?? null"
            [alt]="t('storeSettings.branding.currentLogoAlt')"
            [busy]="uploading() === 'logo'"
            (picked)="picked.emit({kind: 'logo', file: $event})"
          />
        </div>

        <div class="slot">
          <p class="field-label">{{ t('storeSettings.branding.banner') }}</p>
          <app-image-picker
            [label]="t('storeSettings.branding.dropOrBrowse')"
            [rules]="bannerRules"
            [url]="branding().banner?.url ?? null"
            [fileName]="branding().banner?.name ?? null"
            [alt]="t('storeSettings.branding.currentBannerAlt')"
            [busy]="uploading() === 'banner'"
            (picked)="picked.emit({kind: 'banner', file: $event})"
          />
        </div>

        <p class="branding-note">{{ t('storeSettings.branding.replaceOnly') }}</p>
      </div>
    </app-panel>
  `,
  styleUrls: ['../settings-card.css', './branding-section.css'],
})
export class BrandingSection {
  readonly branding = input.required<BrandingSettings>();
  readonly storeName = input.required<string>();
  /** Which upload is in flight, so the well that is busy is the one that says so. */
  readonly uploading = input<'logo' | 'banner' | 'slide' | null>(null);

  readonly picked = output<{kind: 'logo' | 'banner'; file: File}>();

  /**
   * What each asset has to be.
   *
   * The numbers are the storefront's, not a house style: the logo is rendered square in the
   * header and the banner fills a wide hero, so anything far from those proportions is a mistake
   * caught here rather than a surprise on the shop. SVG is allowed for the logo alone — a vector
   * banner is not a thing anyone has.
   */
  protected readonly logoRules: ImageRules = {
    accept: 'image/png,image/svg+xml',
    maxBytes: 2 * 1024 * 1024,
    minWidth: 512,
    minHeight: 512,
    aspect: 1,
    aspectTolerance: 0.2,
  };

  protected readonly bannerRules: ImageRules = {
    accept: 'image/jpeg,image/png',
    maxBytes: 5 * 1024 * 1024,
    minWidth: 1200,
    minHeight: 300,
    aspect: 4,
    aspectTolerance: 0.35,
  };

  protected readonly initial = computed(() => this.storeName().charAt(0).toUpperCase());
}
