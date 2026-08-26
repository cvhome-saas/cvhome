import {Component, computed, inject, input, signal} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import type {ReferenceOption} from '@core/reference/reference-data.service';
import type {MediaAsset} from '@models/content';
import {BusyOverlay} from '@shared/ui/busy-overlay/busy-overlay';
import {FormField} from '@shared/ui/form-field/form-field';
import {Icon} from '@shared/ui/icon/icon';
import {LoadError} from '@shared/ui/load-error/load-error';
import {LocaleSwitcher} from '@shared/ui/locale-switcher/locale-switcher';
import {Panel} from '@shared/ui/panel/panel';
import {TextField} from '@shared/ui/text-field/text-field';
import {
  BRANDING_SLOTS,
  BrandingFacade,
  SEO_FIELDS,
  type BrandingSlot,
  type SeoField,
} from '../../facades/branding.facade';
import {MediaPickerDialog} from '../media-picker/media-picker-dialog';

/**
 * "How the store looks": the brand images, the social links and the title and description search
 * engines show for the store.
 *
 * All three used to live in store management, on the merchant service, and the copy lived in a
 * `LANDING_PAGE` snippet on this one. The images come from the media library now, which is what
 * makes them removable — merchant only ever had upload endpoints, so a logo could be set and never
 * cleared.
 */
@Component({
  selector: 'app-branding-tab',
  imports: [
    BusyOverlay,
    FormField,
    Icon,
    LoadError,
    LocaleSwitcher,
    MediaPickerDialog,
    Panel,
    TextField,
    TranslocoDirective,
  ],
  templateUrl: './branding-tab.html',
  styleUrl: './branding-tab.css',
})
export class BrandingTab {
  protected readonly facade = inject(BrandingFacade);

  readonly locales = input.required<readonly ReferenceOption[]>();
  readonly defaultLocale = input.required<string>();
  readonly canManage = input(true);

  protected readonly slots = BRANDING_SLOTS;
  protected readonly seoFields = SEO_FIELDS;

  protected readonly language = signal<string>('');
  protected readonly activeLanguage = computed(() => this.language() || this.defaultLocale());

  /** Which slot the picker is filling, or null when it is closed. */
  protected readonly picking = signal<BrandingSlot | null>(null);

  /** Languages that already have something written, for the switcher's dots. */
  protected readonly filled = computed<ReadonlySet<string>>(() => {
    const draft = this.facade.draft();
    const set = new Set<string>();
    if (draft) {
      for (const field of SEO_FIELDS) {
        for (const [locale, value] of Object.entries(draft.seo[field])) {
          if (value.trim()) {
            set.add(locale);
          }
        }
      }
    }
    return set;
  });

  protected url(slot: BrandingSlot): string | null {
    return this.facade.branding()?.[slot]?.url ?? null;
  }

  /** True while the draft still names the asset the thumbnail is showing. */
  protected hasMedia(slot: BrandingSlot): boolean {
    const draft = this.facade.draft();
    if (!draft) {
      return false;
    }
    return draft[`${slot === 'og' ? 'og' : slot}MediaId` as keyof typeof draft] !== null;
  }

  protected seoValue(field: SeoField): string {
    return this.facade.draft()?.seo[field][this.activeLanguage()] ?? '';
  }

  protected onSeo(field: SeoField, value: string): void {
    this.facade.setSeo(field, this.activeLanguage(), value);
  }

  protected linkValue(provider: string): string {
    return this.facade.draft()?.socialLinks[provider] ?? '';
  }

  protected onPicked(asset: MediaAsset): void {
    const slot = this.picking();
    this.picking.set(null);
    if (slot) {
      this.facade.setMedia(slot, asset.id);
    }
  }
}
