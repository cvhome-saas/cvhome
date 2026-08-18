import {DestroyRef, Injectable, computed, effect, inject, linkedSignal, signal, untracked} from '@angular/core';
import {rxResource, takeUntilDestroyed, toSignal} from '@angular/core/rxjs-interop';
import {FormGroup} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';

import {ApiErrorService} from '@core/errors/api-error.service';
import {clearServerErrorsOnChange} from '@core/errors/form-error.utils';
import {CONSOLE_LOCALES, LocaleService} from '@core/i18n/locale.service';
import {
  SECTIONS,
  type DomainStatus,
  type LocaleCode,
  type SettingsSection,
  type SettingsSectionKey,
  type StoreSettings,
} from '@models/store-settings';
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
@Injectable({providedIn: 'root'})
export class StoreSettingsFacade {
  private readonly api = inject(StoreSettingsApi);
  private readonly formService = inject(StoreSettingsFormService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly locale = inject(LocaleService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly transloco = inject(TranslocoService);

  /** Which card is open. Set from the route param, so a section is linkable. */
  readonly activeSection = signal<SettingsSectionKey>('branding');

  /** Which language the home-page copy is being written in. */
  readonly activeLanguage = signal<LocaleCode>(this.locale.currentLocale().code);

  readonly languages = CONSOLE_LOCALES;

  readonly form: SettingsForm = this.formService.create();

  private readonly snapshot = rxResource({stream: () => this.api.loadSettings()});

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

  /** How the custom domain's last check went. Not in the form — it is state, not input. */
  readonly domainStatus = linkedSignal<StoreSettings | undefined, DomainStatus>({
    source: () => this.loaded(),
    computation: (settings) => this.customDomain(settings)?.status ?? 'unverified',
  });

  readonly customDomainRecord = computed(() => this.customDomain(this.loaded())?.record ?? null);

  readonly subdomain = computed(
    () => this.loaded()?.domains.find((entry) => entry.type === 'SUB_DOMAIN')?.domain ?? '',
  );

  /** `Store · domain`, under the page title. */
  readonly context = computed(() => {
    const settings = this.loaded();
    return settings ? `${settings.storeName} · ${this.subdomain()}` : null;
  });

  readonly isPublished = computed(() => this.loaded()?.details.published ?? false);

  /**
   * The sub-nav. `attention` is real state — a custom domain that has been typed but not
   * verified — rather than the mockup's hardcoded flag.
   */
  readonly sections = computed<readonly SettingsSection[]>(() => {
    const pending = this.domainStatus();
    const needsDomain = pending !== 'verified' && this.customDomain(this.loaded()) !== undefined;
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

  /** Puts the forms back to the last saved document. */
  discard(): void {
    const settings = this.loaded();
    if (settings) {
      this.formService.reset(this.form, settings);
    }
  }

  /**
   * Runs a DNS check. The stream emits `checking` first and the outcome second, so the panel
   * shows the intermediate state without owning a timer.
   */
  verifyDomain(): void {
    const domain = this.form.controls.domain.controls.customDomain.value.trim();
    if (!domain) {
      this.toast.warning(this.transloco.translate('storeSettings.domain.enterBeforeChecking'));
      return;
    }

    this.api
      .verifyDomain(domain)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((status) => this.domainStatus.set(status));
  }

  labelOf(key: SettingsSectionKey): string {
    const labelKey = SECTIONS.find((section) => section.key === key)?.labelKey;
    return this.transloco.translate(labelKey ?? 'storeSettings.nav.heading');
  }

  private customDomain(settings: StoreSettings | undefined) {
    return settings?.domains.find((entry) => entry.type === 'CUSTOM_DOMAIN');
  }
}
