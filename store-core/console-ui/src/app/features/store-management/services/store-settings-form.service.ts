import {Injectable, inject} from '@angular/core';
import {FormControl, FormGroup, NonNullableFormBuilder, Validators} from '@angular/forms';

import {CONSOLE_LOCALES} from '@core/i18n/locale.service';
import {
  CUSTOM_DOMAIN_PATTERN,
  HOME_TITLE_MAX,
  SHORT_DESCRIPTION_MAX,
  SLUG_PATTERN,
  type SettingsSectionKey,
  type StoreSettings,
} from '@models/store-settings';

/** One language's landing copy. */
export type HomeCopyForm = FormGroup<{
  title: FormControl<string>;
  text: FormControl<string>;
  metaDescription: FormControl<string>;
  tags: FormControl<readonly string[]>;
}>;

export type DomainForm = FormGroup<{customDomain: FormControl<string>}>;

export type DetailsForm = FormGroup<{
  name: FormControl<string>;
  legalName: FormControl<string>;
  slug: FormControl<string>;
  category: FormControl<string>;
  supportEmail: FormControl<string>;
  supportPhone: FormControl<string>;
  currency: FormControl<string>;
  language: FormControl<string>;
  timezone: FormControl<string>;
  taxNumber: FormControl<string>;
  address: FormControl<string>;
  shortDescription: FormControl<string>;
  published: FormControl<boolean>;
  maintenanceMode: FormControl<boolean>;
}>;

/** A provider's own credentials. The secret is not here — it never comes back to be edited. */
export type LoginProviderForm = FormGroup<{
  enabled: FormControl<boolean>;
  appId: FormControl<string>;
}>;

export type GatewayForm = FormGroup<{
  enabled: FormControl<boolean>;
  apiKey: FormControl<string>;
}>;

/** Sections with nothing editable still carry a group, so every key resolves to a form. */
export type EmptyForm = FormGroup<Record<string, never>>;

/*
 * The keyed groups. How many keys each has is the server's answer rather than a constant, so
 * they are indexed rather than spelled out — `reset` brings them in line with what arrived.
 */
export type HomeForm = FormGroup<Record<string, HomeCopyForm>>;
export type SocialLinksForm = FormGroup<Record<string, FormControl<string>>>;
export type SocialLoginForm = FormGroup<Record<string, LoginProviderForm>>;
export type PaymentsForm = FormGroup<Record<string, GatewayForm>>;

export interface SettingsForms {
  branding: EmptyForm;
  home: HomeForm;
  domain: DomainForm;
  social: SocialLinksForm;
  slider: EmptyForm;
  details: DetailsForm;
  'social-login': SocialLoginForm;
  payments: PaymentsForm;
}

export type SettingsForm = FormGroup<SettingsForms>;

/**
 * Builds the settings page's forms, so the page never injects a `FormBuilder`.
 *
 * One root group with a child per section: the page-level *Save changes* acts on the active
 * section's child, and the root is what `clearServerErrorsOnChange` and
 * `ApiErrorService.applyToForm` are handed.
 *
 * Every group exists before any data arrives — the shape is known, only the values are not —
 * so the template can bind to controls while the first request is still in flight.
 */
@Injectable({providedIn: 'root'})
export class StoreSettingsFormService {
  private readonly fb = inject(NonNullableFormBuilder);

  /*
   * The containers are constructed rather than built through `fb`: they hold groups, not raw
   * values, so the builder's non-nullable inference has nothing to add and only obscures the
   * declared shape. Leaves still go through `fb`, which is what makes them non-nullable.
   */
  create(): SettingsForm {
    return new FormGroup<SettingsForms>({
      branding: new FormGroup<Record<string, never>>({}),
      home: new FormGroup<HomeForm['controls']>(
        // The console's own locale list is the only list of languages; there is no second one.
        Object.fromEntries(CONSOLE_LOCALES.map((locale) => [locale.code, this.homeCopy()])),
      ),
      domain: this.fb.group({
        customDomain: this.fb.control('', [Validators.pattern(CUSTOM_DOMAIN_PATTERN)]),
      }),
      social: new FormGroup<SocialLinksForm['controls']>({}),
      slider: new FormGroup<Record<string, never>>({}),
      details: this.details(),
      'social-login': new FormGroup<SocialLoginForm['controls']>({}),
      payments: new FormGroup<PaymentsForm['controls']>({}),
    });
  }

  /**
   * Fills the forms from a loaded document and marks them pristine.
   *
   * The provider-keyed groups are built here rather than in `create`, because how many
   * providers there are is the server's answer, not a constant. Reset is where they arrive.
   */
  reset(form: SettingsForm, settings: StoreSettings): void {
    for (const locale of CONSOLE_LOCALES) {
      const copy = settings.home[locale.code];
      form.controls.home.controls[locale.code].reset({
        title: copy?.title ?? '',
        text: copy?.text ?? '',
        metaDescription: copy?.metaDescription ?? '',
        tags: copy?.tags ?? [],
      });
    }

    const custom = settings.domains.find((entry) => entry.type === 'CUSTOM_DOMAIN');
    form.controls.domain.reset({customDomain: custom?.domain ?? ''});

    this.syncKeys(
      form.controls.social,
      settings.socialLinks.map((link) => link.provider),
      () => this.fb.control('', [Validators.pattern(/^[^\s/]+\.[^\s]+$/)]),
    );
    for (const link of settings.socialLinks) {
      form.controls.social.controls[link.provider].reset(link.url);
    }

    const login = form.controls['social-login'];
    this.syncKeys(
      login,
      settings.socialLogin.map((config) => config.providerId),
      () => this.fb.group({enabled: this.fb.control(false), appId: this.fb.control('')}),
    );
    for (const config of settings.socialLogin) {
      login.controls[config.providerId].reset({enabled: config.enabled, appId: config.appId});
    }

    const payments = form.controls.payments;
    this.syncKeys(
      payments,
      settings.payments.map((gateway) => gateway.paymentType),
      () => this.fb.group({enabled: this.fb.control(false), apiKey: this.fb.control('')}),
    );
    for (const gateway of settings.payments) {
      payments.controls[gateway.paymentType].reset({
        enabled: gateway.enabled,
        apiKey: gateway.credentials?.apiKey ?? '',
      });
    }

    form.controls.details.reset({...settings.details});
  }

  private homeCopy(): HomeCopyForm {
    return this.fb.group({
      // The mockup's own hint. Over-length is an error, not a warning: it is the browser tab title.
      title: this.fb.control('', [Validators.required, Validators.maxLength(HOME_TITLE_MAX)]),
      text: this.fb.control(''),
      /*
       * 150–160 characters is a recommendation, not a constraint — a short meta description is
       * worse for search and perfectly valid. The section shows a counter instead of an error.
       */
      metaDescription: this.fb.control(''),
      tags: this.fb.control<readonly string[]>([]),
    });
  }

  private details(): DetailsForm {
    return this.fb.group({
      name: this.fb.control('', [Validators.required]),
      legalName: this.fb.control('', [Validators.required]),
      slug: this.fb.control('', [Validators.required, Validators.pattern(SLUG_PATTERN)]),
      category: this.fb.control(''),
      supportEmail: this.fb.control('', [Validators.required, Validators.email]),
      supportPhone: this.fb.control(''),
      currency: this.fb.control('', [Validators.required]),
      language: this.fb.control('', [Validators.required]),
      timezone: this.fb.control(''),
      taxNumber: this.fb.control(''),
      address: this.fb.control(''),
      shortDescription: this.fb.control('', [Validators.maxLength(SHORT_DESCRIPTION_MAX)]),
      published: this.fb.control(false),
      maintenanceMode: this.fb.control(false),
    });
  }

  /**
   * Brings a keyed group in line with the keys the server sent — adding what is new, removing
   * what is gone. Existing controls are left in place so a reset does not rebuild the tree
   * the template is already bound to.
   */
  private syncKeys<T extends FormGroup | FormControl>(
    group: FormGroup<Record<string, T>>,
    keys: readonly string[],
    make: () => T,
  ): void {
    for (const key of keys) {
      if (!group.contains(key)) {
        group.addControl(key, make());
      }
    }
    for (const key of Object.keys(group.controls)) {
      if (!keys.includes(key)) {
        // `removeControl` narrows its name to optional keys, which an index signature has none of.
        (group as FormGroup).removeControl(key);
      }
    }
  }
}

/** The section a save posts, for the api service's patch. */
export function sectionValueOf(form: SettingsForm, key: SettingsSectionKey): Record<string, unknown> {
  return (form.controls[key] as FormGroup).getRawValue() as Record<string, unknown>;
}
