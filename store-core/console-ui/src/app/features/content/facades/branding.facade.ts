import {Injectable, computed, effect, inject, signal} from '@angular/core';
import {rxResource} from '@angular/core/rxjs-interop';
import {TranslocoService} from '@jsverse/transloco';

import {SiteSettingsService} from '@api/content/site-settings.service';
import {ManagerStoreService} from '@api/tenancy/manager-store.service';
import {ApiErrorService} from '@core/errors/api-error.service';
import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';
import type {SiteBranding, SiteSettings, SocialLink} from '@models/content';
import {socialLinkProblem} from '@models/social-links';
import {ToastService} from '@shared/ui/toast/toast';

/** The SEO fields the store writes about itself, in the order the form shows them. */
export const SEO_FIELDS = ['metaTitle', 'metaDescription'] as const;

export type SeoField = (typeof SEO_FIELDS)[number];

/** Which brand image a slot holds. The keys are the server's, so they double as the write path. */
export const BRANDING_SLOTS = ['logo', 'logoDark', 'favicon', 'og'] as const;

export type BrandingSlot = (typeof BRANDING_SLOTS)[number];

const SLOT_FIELD: Readonly<Record<BrandingSlot, keyof SiteSettings>> = {
  logo: 'logoMediaId',
  logoDark: 'logoDarkMediaId',
  favicon: 'faviconMediaId',
  og: 'ogMediaId',
};

/** The editable copy of the record: media ids, per-locale SEO, and one url per provider. */
export interface BrandingDraft {
  logoMediaId: number | null;
  logoDarkMediaId: number | null;
  faviconMediaId: number | null;
  ogMediaId: number | null;
  seo: Record<SeoField, Record<string, string>>;
  socialLinks: Record<string, string>;
}

function toDraft(settings: SiteSettings, providers: readonly string[]): BrandingDraft {
  const links: Record<string, string> = {};
  for (const provider of providers) {
    links[provider] = '';
  }
  for (const link of settings.socialLinks ?? []) {
    links[link.provider] = link.url;
  }
  const seo = {} as Record<SeoField, Record<string, string>>;
  for (const field of SEO_FIELDS) {
    seo[field] = {...(settings.seo?.[field] ?? {})};
  }
  return {
    logoMediaId: settings.logoMediaId ?? null,
    logoDarkMediaId: settings.logoDarkMediaId ?? null,
    faviconMediaId: settings.faviconMediaId ?? null,
    ogMediaId: settings.ogMediaId ?? null,
    seo,
    socialLinks: links,
  };
}

/**
 * How the store looks: its brand imagery, its social links and the title and description search
 * engines show for it.
 *
 * These used to be two screens on two services — merchant's branding, slider and social-link
 * sections, and a `LANDING_PAGE` snippet for the copy. One record now, and one save.
 */
@Injectable()
export class BrandingFacade {
  private readonly api = inject(SiteSettingsService);
  private readonly stores = inject(ManagerStoreService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);
  private readonly shell = inject(ConsoleShellFacade);

  private readonly resource = rxResource({
    params: () => this.shell.currentStoreId() ?? undefined,
    stream: () => this.api.get(),
  });

  /**
   * The providers a store may link to. Degrades to whatever it has already saved: a reference-data
   * outage should cost the section its empty rows, not the links the operator can already see.
   */
  private readonly providersResource = rxResource({
    params: () => this.shell.currentStoreId() ?? undefined,
    stream: () => this.stores.socialLinkProviders(),
  });

  readonly isLoading = this.resource.isLoading;
  readonly error = computed(() => this.resource.error() as Error | undefined);
  readonly busy = signal(false);
  readonly dirty = signal(false);

  readonly draft = signal<BrandingDraft | null>(null);

  /** The resolved images, for the thumbnails. Server-side, so it lags the draft until a save. */
  readonly branding = computed<SiteBranding | null>(() =>
    this.resource.hasValue() ? (this.resource.value().branding ?? null) : null,
  );

  /**
   * Whether every social link is a profile on the provider whose row it is in.
   *
   * Save is blocked rather than the bad value being dropped on the way out: a link silently
   * discarded at save time looks to the operator exactly like one that saved.
   */
  readonly linksValid = computed<boolean>(() => {
    const draft = this.draft();
    return !draft || Object.entries(draft.socialLinks)
      .every(([provider, url]) => socialLinkProblem(provider, url) === null);
  });

  readonly providers = computed<readonly string[]>(() => {
    if (this.providersResource.hasValue()) {
      return this.providersResource.value();
    }
    const draft = this.draft();
    return draft ? Object.keys(draft.socialLinks) : [];
  });

  constructor() {
    effect(() => {
      const settings = this.resource.hasValue() ? this.resource.value() : null;
      const providers = this.providersResource.hasValue() ? this.providersResource.value() : [];
      if (settings) {
        this.draft.set(toDraft(settings, providers));
        this.dirty.set(false);
      }
    });
  }

  /** Sets or clears one brand image. A `null` id is how a logo is removed. */
  setMedia(slot: BrandingSlot, id: number | null): void {
    this.patch({[SLOT_FIELD[slot]]: id} as Partial<BrandingDraft>);
  }

  setSeo(field: SeoField, locale: string, value: string): void {
    const draft = this.draft();
    if (!draft) {
      return;
    }
    this.patch({seo: {...draft.seo, [field]: {...draft.seo[field], [locale]: value}}});
  }

  setSocialLink(provider: string, url: string): void {
    const draft = this.draft();
    if (!draft) {
      return;
    }
    this.patch({socialLinks: {...draft.socialLinks, [provider]: url}});
  }

  save(): void {
    const draft = this.draft();
    if (!draft) {
      return;
    }
    this.busy.set(true);
    this.api.put(this.toWire(draft)).subscribe({
      next: () => {
        this.busy.set(false);
        this.dirty.set(false);
        // The thumbnails come from the server's resolved branding, so a save has to re-read.
        this.resource.reload();
        this.toast.success(this.transloco.translate('content.branding.saved'));
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }

  discard(): void {
    this.resource.reload();
  }

  retry(): void {
    this.resource.reload();
  }

  private patch(change: Partial<BrandingDraft>): void {
    const draft = this.draft();
    if (!draft) {
      return;
    }
    this.draft.set({...draft, ...change});
    this.dirty.set(true);
  }

  /**
   * The whole record every time: the server replaces rather than merges, which is what makes an
   * emptied field genuinely clear that value instead of leaving the old one in place.
   */
  private toWire(draft: BrandingDraft): SiteSettings {
    const socialLinks: SocialLink[] = Object.entries(draft.socialLinks)
      .filter(([, url]) => url.trim().length > 0)
      .map(([provider, url]) => ({provider, url: url.trim()}));
    const seo: Record<string, Record<string, string>> = {};
    for (const field of SEO_FIELDS) {
      const written = Object.entries(draft.seo[field]).filter(([, value]) => value.trim().length > 0);
      if (written.length > 0) {
        seo[field] = Object.fromEntries(written.map(([locale, value]) => [locale, value.trim()]));
      }
    }
    return {
      logoMediaId: draft.logoMediaId,
      logoDarkMediaId: draft.logoDarkMediaId,
      faviconMediaId: draft.faviconMediaId,
      ogMediaId: draft.ogMediaId,
      seo,
      socialLinks,
    };
  }
}
