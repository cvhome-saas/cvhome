import {DestroyRef, Injectable, computed, effect, inject, linkedSignal, signal, untracked} from '@angular/core';
import {rxResource, takeUntilDestroyed, toSignal} from '@angular/core/rxjs-interop';
import {forkJoin, map, timer} from 'rxjs';
import {FormGroup} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';

import {DOCUMENT} from '@angular/common';

import {ConsoleShellFacade} from '@layouts/console-shell/facades/console-shell.facade';

import {ApiErrorService} from '@core/errors/api-error.service';
import {clearServerErrorsOnChange} from '@core/errors/form-error.utils';
import {LocaleService} from '@core/i18n/locale.service';
import {
  SECTIONS,
  type DnsRecord,
  type DomainStatus,
  type SettingsSection,
  type SettingsSectionKey,
  type SliderSlide,
  type StoreDomain,
  type StoreSettings,
} from '@models/store-settings';
import {ReferenceDataService, type ReferenceOption} from '@core/reference/reference-data.service';
import {ToastService} from '@shared/ui/toast/toast';
import {StoreSettingsApi} from '../services/store-settings.api.service';
import {
  StoreSettingsFormService,
  sectionValueOf,
  type SettingsForm,
} from '../services/store-settings-form.service';

/**
 * The settings page's data and its forms.
 *
 * Follows `OrdersFacade`: an `rxResource` for the load, a `linkedSignal` holding the last
 * good snapshot so the page does not blank between requests, and `isLoading` / `error` /
 * `retry()` with the same meanings.
 *
 * What is different is that this page writes. The forms live here rather than in the
 * component, because *Save changes* sits in the page header while the fields it saves are in
 * a section component several levels down — one owner, reachable from both.
 */
/** How long the "Checking…" state stays up, however fast the resolver is. */
const CHECK_MIN_VISIBLE_MS = 400;

/** The same, for an upload — long enough that the well's spinner and its tick both register. */
const UPLOAD_MIN_VISIBLE_MS = 600;

@Injectable()
export class StoreSettingsFacade {
  private readonly api = inject(StoreSettingsApi);
  private readonly formService = inject(StoreSettingsFormService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly locale = inject(LocaleService);
  private readonly shell = inject(ConsoleShellFacade);
  private readonly reference = inject(ReferenceDataService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly transloco = inject(TranslocoService);
  private readonly document = inject(DOCUMENT);

  /** Which card is open. Set from the route param, so a section is linkable. */
  readonly activeSection = signal<SettingsSectionKey>('branding');

  /**
   * The languages the storefront's landing copy can be written in.
   *
   * The *store's* supported set, plus any language the landing box already holds copy for. Not the
   * console's own en/ar locales, which is what this used to be: those say which language the
   * operator reads the console in, and have nothing to do with which languages their shoppers are
   * served. `ReferenceDataService` names them, so the track reads "English", not "en".
   */
  readonly languages = computed<readonly ReferenceOption[]>(() => {
    const settings = this.loaded();
    if (!settings) {
      return [];
    }
    const codes = [
      ...new Set([...settings.details.supportedLanguages, ...Object.keys(settings.home)]),
    ];
    return codes.map((code) => ({code, label: this.reference.languageName(code)}));
  });

  /**
   * Which language the home-page copy is being written in.
   *
   * Follows the store's default language once one has loaded, rather than the console's — the copy
   * a seller writes first is the one their storefront shows by default. `linkedSignal` so an
   * explicit choice survives a reload but a store swap starts from that store's default.
   */
  readonly activeLanguage = linkedSignal<readonly ReferenceOption[], string>({
    source: this.languages,
    computation: (languages, previous) => {
      const chosen = previous?.value;
      if (chosen && languages.some((language) => language.code === chosen)) {
        return chosen;
      }
      const preferred = this.loaded()?.details.language ?? this.locale.currentLocale().code;
      return languages.some((language) => language.code === preferred)
        ? preferred
        : (languages[0]?.code ?? preferred);
    },
  });

  readonly form: SettingsForm = this.formService.create();

  /**
   * The custom-domain field could not reach a resolver, so its check neither passed nor failed.
   *
   * Surfaced from the form service because the field's own validator is what discovers it, and the
   * section needs to say so — it is the one outcome that is not expressible as a validation error
   * without locking the field.
   */
  readonly dnsCheckUnavailable = this.formService.dnsCheckUnavailable;

  /**
   * The settings document, keyed by the store it belongs to.
   *
   * `params` is not decoration. Without it the resource loads once and never again, so switching
   * stores from the rail left the whole page showing the previous store's settings — its domains,
   * its landing copy, its gateway secrets — under the new store's name in the request context. The
   * next save would then have written those values onto the other store. The dashboard already
   * keys its resource this way and says why: a page that keeps one store's data under another
   * store's name is the worst kind of wrong, because it looks fine.
   */
  private readonly snapshot = rxResource({
    params: () => this.shell.currentStoreId() ?? undefined,
    stream: () => this.api.loadSettings(),
  });

  /**
   * The last document that loaded successfully.
   *
   * A resource clears its value while the next request is in flight, which would empty the
   * forms on every reload. `hasValue()` guards the read: `value()` throws in an error state,
   * and a failed refresh should leave the last good settings on screen.
   */
  private readonly loaded = linkedSignal<StoreSettings | undefined, StoreSettings | undefined>({
    source: () => (this.snapshot.hasValue() ? this.snapshot.value() : undefined),
    computation: (incoming, previous) => incoming ?? previous?.value,
  });

  readonly isLoading = this.snapshot.isLoading;
  readonly error = computed(() => this.snapshot.error() as Error | undefined);
  /** True only before the first response, when there is nothing to show yet. */
  readonly isEmpty = computed(() => this.loaded() === undefined);

  readonly settings = this.loaded;
  readonly isSaving = signal(false);
  readonly isDeleting = signal(false);
  /** An upload in flight, named so the section can show which tile is busy. */
  readonly uploading = signal<'logo' | 'banner' | 'slide' | null>(null);

  /**
   * Bumped by every form event — value, status, touched and pristine changes alike.
   *
   * `dirty` and `valid` are not signals, so a computed reading them would never recompute.
   * This is the dependency that makes *Save changes* enable and disable as the operator types.
   */
  private readonly formEvent = toSignal(this.form.events, {initialValue: null});

  /** The active section's own group — what *Save changes* acts on. */
  readonly sectionForm = computed<FormGroup>(
    () => this.form.controls[this.activeSection()] as FormGroup,
  );

  readonly isDirty = computed(() => {
    this.formEvent();
    return this.sectionForm().dirty;
  });

  readonly canSave = computed(() => {
    this.formEvent();
    const form = this.sectionForm();
    return form.dirty && form.valid && !this.isSaving();
  });

  /**
   * How each domain's last DNS check went, keyed by hostname.
   *
   * A map rather than one value, because a store may have any number of custom domains and each is
   * checked on its own. Cleared on every new document: a check describes DNS at the moment it ran, and
   * carrying an old verdict across a reload would state something the console no longer knows.
   */
  readonly domainStatus = linkedSignal<StoreSettings | undefined, ReadonlyMap<string, DomainStatus>>({
    source: () => this.loaded(),
    computation: () => new Map<string, DomainStatus>(),
  });

  /** The domain currently being looked up, so its row can show a spinner and refuse a second click. */
  readonly checkingDomain = signal<string | null>(null);

  readonly domains = computed<readonly StoreDomain[]>(() => this.loaded()?.domains ?? []);

  readonly customDomains = computed(() =>
    this.domains().filter((entry) => entry.type === 'CUSTOM_DOMAIN'),
  );

  readonly subdomain = computed(() => {
    const entry = this.domains().find((candidate) => candidate.type === 'SUB_DOMAIN');
    return entry?.hostname ?? entry?.domain ?? '';
  });

  /**
   * The CNAME the operator has to add, for whatever is currently typed in the field.
   *
   * `null` when the platform could not tell us where to point — the pod lookup is refused for a
   * suspended store — because instructions that name no target are worse than none.
   */
  readonly customDomainRecord = computed<DnsRecord | null>(() => {
    this.formEvent();
    /*
     * `activeLang()` is a dependency, not decoration: the placeholder name is translated, and a
     * `computed` that calls `translate()` without tracking the language keeps whatever it resolved
     * on first evaluation — the record read "your domain" in the middle of an Arabic page.
     */
    this.transloco.activeLang();
    const target = this.loaded()?.podTarget;
    const typed = this.form.controls.domain.controls.customDomain.value.trim();
    if (!target) {
      return null;
    }
    return {
      type: 'CNAME',
      name: typed || this.transloco.translate('storeSettings.domain.yourDomain'),
      value: target,
    };
  });

  /** `Store · domain`, under the page title. */
  readonly context = computed(() => {
    const settings = this.loaded();
    return settings ? `${settings.storeName} · ${this.subdomain()}` : null;
  });

  /**
   * The sub-nav. `attention` is real state — a custom domain that has been typed but not
   * verified — rather than the mockup's hardcoded flag.
   */
  readonly sections = computed<readonly SettingsSection[]>(() => {
    const checked = this.domainStatus();
    /*
     * A domain the operator has looked up and found wrong or still propagating. An unchecked domain
     * raises nothing: the console has not looked, so it has nothing to report — the fixture's version
     * of this flagged every store that had a custom domain at all.
     */
    const needsDomain = this.customDomains().some((entry) => {
      const status = checked.get(entry.domain);
      return status === 'failed' || status === 'waiting';
    });
    return SECTIONS.map((section) => ({
      ...section,
      attention: section.key === 'domain' && needsDomain,
    }));
  });

  /** The document the forms were last filled from, so a re-fill only happens on new data. */
  private synced: StoreSettings | undefined;

  constructor() {
    // A server error is not a validator: nothing else would ever clear it.
    clearServerErrorsOnChange(this.form, this.destroyRef);

    effect(() => {
      const settings = this.loaded();
      if (!settings || settings === this.synced) {
        return;
      }
      untracked(() => {
        this.synced = settings;
        this.formService.reset(this.form, settings);
      });
    });
  }

  retry(): void {
    this.snapshot.reload();
  }

  /**
   * Saves the active section.
   *
   * On success the server's answer replaces the snapshot, which re-fills the forms and makes
   * them pristine again. On failure the forms are left exactly as they were — dirty, with the
   * operator's text still in them — and the field errors the server named are bound to their
   * controls.
   */
  save(): void {
    const key = this.activeSection();
    const form = this.sectionForm();

    if (!form.dirty || this.isSaving()) {
      return;
    }
    if (form.invalid) {
      form.markAllAsTouched();
      this.toast.warning(this.transloco.translate('errors.category.validation'));
      return;
    }

    this.isSaving.set(true);
    this.api
      .saveSection(key, sectionValueOf(this.form, key))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (settings) => {
          this.isSaving.set(false);
          this.snapshot.set(settings);
          this.toast.success(
            this.transloco.translate('storeSettings.sectionSaved', {section: this.labelOf(key)}),
          );
        },
        error: (failure: unknown) => {
          this.isSaving.set(false);
          this.apiErrors.applyToForm(failure, form);
        },
      });
  }

  /**
   * Uploads a logo or a banner.
   *
   * Sent the moment a file is chosen rather than held for *Save changes*: the endpoint is a
   * multipart POST of its own, so there is nothing for the section's (empty) form to submit, and
   * an image that appears only after a separate save is a confusing thing to offer.
   */
  upload(kind: 'logo' | 'banner', file: File): void {
    if (this.uploading()) {
      return;
    }
    this.uploading.set(kind);

    /*
     * Held for a beat, the same way the DNS check is. A small image on a local pod round-trips
     * faster than the eye registers a spinner, so without this the well flickered and an operator
     * could not tell an upload that ran from a click that missed.
     */
    const request = kind === 'logo' ? this.api.uploadLogo(file) : this.api.uploadBanner(file);
    forkJoin({settings: request, _visible: timer(UPLOAD_MIN_VISIBLE_MS)})
      .pipe(
        map((answer) => answer.settings),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
      next: (settings) => {
        this.uploading.set(null);
        this.snapshot.set(settings);
        this.toast.success(this.transloco.translate(`storeSettings.branding.${kind}Uploaded`));
      },
      error: (failure: unknown) => {
        this.uploading.set(null);
        this.toast.danger(this.apiErrors.messageFor(failure));
      },
    });
  }

  /**
   * Deletes the store, then sends the operator somewhere that still exists.
   *
   * The server refuses to remove an org's default store, so a failure here is usually a rule
   * rather than a fault — the message it returns says which, and is shown rather than replaced
   * with a generic one.
   */
  deleteStore(): void {
    if (this.isDeleting()) {
      return;
    }
    this.isDeleting.set(true);

    this.api
      .deleteStore()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.isDeleting.set(false);
          this.toast.success(this.transloco.translate('storeSettings.details.deleteStoreDone'));
          /*
           * The store this page is scoped to is gone, so staying here would keep querying it. A
           * full reload is deliberate: the store list, the request context and the rail all read
           * the deleted store, and re-resolving them is what the console-context guard does on a
           * fresh navigation.
           */
          this.document.defaultView?.location.assign('/');
        },
        error: (failure: unknown) => {
          this.isDeleting.set(false);
          this.toast.danger(this.apiErrors.messageFor(failure));
        },
      });
  }

  /** Puts the forms back to the last saved document. */
  discard(): void {
    const settings = this.loaded();
    if (settings) {
      this.formService.reset(this.form, settings);
    }
  }

  /**
   * Looks one domain's CNAME up.
   *
   * Takes the hostname explicitly rather than reading the form, because the rows check an *allocated*
   * domain while the field checks one that is about to be added — same lookup, two callers. A lookup
   * that fails is reported as a failure to check, not as a failed domain: the console does not know
   * that the operator's DNS is wrong, only that it could not find out.
   */
  verifyDomain(domain: string): void {
    const hostname = domain.trim();
    if (!hostname) {
      this.toast.warning(this.transloco.translate('storeSettings.domain.enterBeforeChecking'));
      return;
    }
    if (this.checkingDomain()) {
      return;
    }

    this.checkingDomain.set(hostname);
    this.setDomainStatus(hostname, 'checking');

    /*
     * Held for a beat. A DoH lookup usually answers in under 30ms, which is faster than the eye
     * reads a label — the button flashed "Checking…" and was back before anyone saw it, so a check
     * that ran and a click that missed looked identical. `forkJoin` with a timer makes the busy
     * state legible without making the answer any later than it already is.
     */
    forkJoin({
      status: this.api.verifyDomain(hostname),
      _visible: timer(CHECK_MIN_VISIBLE_MS),
    })
      .pipe(
        map((answer) => answer.status),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (status) => {
          this.checkingDomain.set(null);
          if (status) {
            this.setDomainStatus(hostname, status);
          } else {
            // Nothing to compare against, so the domain keeps whatever it had — including nothing.
            this.clearDomainStatus(hostname);
            this.toast.warning(this.transloco.translate('storeSettings.domain.checkFailed'));
          }
        },
        error: () => {
          this.checkingDomain.set(null);
          /*
           * A lookup that could not be made leaves no verdict, rather than leaving a stale or
           * invented one. The row goes back to saying nothing and the toast says why.
           */
          this.clearDomainStatus(hostname);
          this.toast.warning(this.transloco.translate('storeSettings.domain.checkFailed'));
        },
      });
  }

  /** Takes a hostname off the store. Only custom domains are offered — the subdomain is the store's address. */
  removeDomain(domain: string): void {
    if (this.isSaving()) {
      return;
    }
    this.isSaving.set(true);

    this.api
      .removeDomain(domain)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (settings) => {
          this.isSaving.set(false);
          this.snapshot.set(settings);
          this.toast.success(this.transloco.translate('storeSettings.domain.removed', {domain}));
        },
        error: (failure: unknown) => {
          this.isSaving.set(false);
          this.toast.danger(this.apiErrors.messageFor(failure));
        },
      });
  }

  /**
   * Adds a slide.
   *
   * Sent on selection rather than held for *Save changes*, for the same reason the logo is: it is a
   * multipart POST of its own and the slider form has no controls to submit. The pod names the file,
   * so the answer has to come from a reload rather than from the upload.
   */
  addSlide(file: File): void {
    if (this.uploading()) {
      return;
    }
    this.uploading.set('slide');

    forkJoin({settings: this.api.addSlide(file), _visible: timer(UPLOAD_MIN_VISIBLE_MS)})
      .pipe(
        map((answer) => answer.settings),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (settings) => {
          this.uploading.set(null);
          this.snapshot.set(settings);
          this.toast.success(this.transloco.translate('storeSettings.slider.uploaded'));
        },
        error: (failure: unknown) => {
          this.uploading.set(null);
          this.toast.danger(this.apiErrors.messageFor(failure));
        },
      });
  }

  /**
   * Replaces the slider with the list the section built — a reorder or a delete.
   *
   * There is no reorder endpoint and no delete-slide endpoint; sending the list you want is the whole
   * API. Applied immediately rather than through *Save changes* so the two act the same way as adding
   * one, and so a half-applied reorder cannot be left sitting in a form.
   */
  saveSlides(slides: readonly SliderSlide[], messageKey: string): void {
    if (this.isSaving()) {
      return;
    }
    this.isSaving.set(true);

    this.api
      .saveSlides(slides)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (settings) => {
          this.isSaving.set(false);
          this.snapshot.set(settings);
          this.toast.success(this.transloco.translate(messageKey));
        },
        error: (failure: unknown) => {
          this.isSaving.set(false);
          this.toast.danger(this.apiErrors.messageFor(failure));
        },
      });
  }

  private setDomainStatus(domain: string, status: DomainStatus): void {
    const next = new Map(this.domainStatus());
    next.set(domain, status);
    this.domainStatus.set(next);
  }

  private clearDomainStatus(domain: string): void {
    const next = new Map(this.domainStatus());
    next.delete(domain);
    this.domainStatus.set(next);
  }

  labelOf(key: SettingsSectionKey): string {
    const labelKey = SECTIONS.find((section) => section.key === key)?.labelKey;
    return this.transloco.translate(labelKey ?? 'storeSettings.nav.heading');
  }

}
