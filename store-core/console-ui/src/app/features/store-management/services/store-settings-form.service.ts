import {Injectable, inject} from '@angular/core';
import {
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  Validators,
  type AbstractControl,
  type ValidationErrors,
  type ValidatorFn,
} from '@angular/forms';

import {CONSOLE_LOCALES} from '@core/i18n/locale.service';
import {
  CUSTOM_DOMAIN_PATTERN,
  HOME_TITLE_MAX,
  PHONE_MIN_DIGITS,
  PHONE_PATTERN,
  SHORT_DESCRIPTION_MAX,
  SLUG_PATTERN,
  UNBACKED_DETAIL_FIELDS,
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

/**
 * The store's identity.
 *
 * The first block saves. The second is `UNBACKED_DETAIL_FIELDS` — present so the designed layout
 * survives, permanently `disabled` so it neither validates nor reaches a request body. Disabling is
 * the enforcement rather than a convention: `sectionValueOf` reads `value`, which omits disabled
 * controls, so there is no route by which these are submitted.
 */
export type DetailsForm = FormGroup<{
  name: FormControl<string>;
  supportEmail: FormControl<string>;
  supportPhone: FormControl<string>;
  currency: FormControl<string>;
  language: FormControl<string>;
  supportedLanguages: FormControl<readonly string[]>;
  country: FormControl<string>;
  addressLine: FormControl<string>;
  city: FormControl<string>;
  postalCode: FormControl<string>;
  stateProvince: FormControl<string>;
  theme: FormControl<string>;
  colorTheme: FormControl<string>;
  inBusinessSince: FormControl<string>;
  dimensionUnit: FormControl<string>;
  weightUnit: FormControl<string>;
  requireLoginForOrderPlacement: FormControl<boolean>;
  useCache: FormControl<boolean>;

  legalName: FormControl<string>;
  slug: FormControl<string>;
  category: FormControl<string>;
  timezone: FormControl<string>;
  taxNumber: FormControl<string>;
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

    /*
     * Emptied, not prefilled. The field adds a domain — the router has no update, only allocate and
     * remove — so seeding it with an existing one invited the operator to "edit" a hostname and get a
     * second allocation instead. The allocated domains are a list above it now.
     */
    form.controls.domain.reset({customDomain: ''});

    this.syncKeys(
      form.controls.social,
      settings.socialLinks.map((link) => link.provider),
      () => this.fb.control('', [Validators.pattern(SOCIAL_URL_PATTERN)]),
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

    const details = settings.details;
    form.controls.details.reset({
      name: details.name,
      supportEmail: details.supportEmail,
      supportPhone: details.supportPhone,
      currency: details.currency,
      language: details.language,
      supportedLanguages: details.supportedLanguages,
      country: details.country,
      addressLine: details.address.address,
      city: details.address.city,
      postalCode: details.address.postalCode,
      stateProvince: details.address.stateProvince,
      theme: details.theme,
      colorTheme: details.colorTheme,
      inBusinessSince: details.inBusinessSince,
      dimensionUnit: details.dimensionUnit,
      weightUnit: details.weightUnit,
      requireLoginForOrderPlacement: details.requireLoginForOrderPlacement,
      useCache: details.useCache,
      legalName: details.legalName,
      slug: details.slug,
      category: details.category,
      timezone: details.timezone,
      taxNumber: details.taxNumber,
      shortDescription: details.shortDescription,
      published: details.published,
      maintenanceMode: details.maintenanceMode,
    });
    /*
     * `reset` re-enables every control it is given a value for, so the unbacked ones are put back
     * how they were declared. Without this a load would quietly make them editable.
     */
    for (const field of UNBACKED_DETAIL_FIELDS) {
      form.controls.details.controls[field].disable({emitEvent: false});
    }
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

  /*
   * `name`, `email` and `phone` are `@NotNull` on `MerchantStoreDetails` and `PUT /private/store`
   * is `@Valid`, so those three are required here because the server rejects the save otherwise —
   * it is kinder to say so before the round trip than to translate a 400 afterwards. `currency`,
   * `language`, `supportedLanguages` and `country` carry no bean validation, but a storefront
   * cannot price, translate or ship without them, and seller-ui required all four as well.
   */
  private details(): DetailsForm {
    return this.fb.group(
      {
        name: this.fb.control('', [Validators.required]),
        supportEmail: this.fb.control('', [Validators.required, Validators.email]),
        supportPhone: this.fb.control('', [Validators.required, phoneNumber]),
        currency: this.fb.control('', [Validators.required]),
        language: this.fb.control('', [Validators.required]),
        /* `Validators.required` counts `[]` as empty, which is exactly the rule: a storefront with no language cannot render. */
        supportedLanguages: this.fb.control<readonly string[]>([], [Validators.required]),
        country: this.fb.control('', [Validators.required]),
        addressLine: this.fb.control(''),
        city: this.fb.control(''),
        postalCode: this.fb.control(''),
        stateProvince: this.fb.control(''),
        theme: this.fb.control(''),
        colorTheme: this.fb.control(''),
        /* `LocalDate`, so the control holds `YYYY-MM-DD` and binds to `<input type="date">`. */
        inBusinessSince: this.fb.control(''),
        dimensionUnit: this.fb.control(''),
        weightUnit: this.fb.control(''),
        requireLoginForOrderPlacement: this.fb.control(false),
        useCache: this.fb.control(false),

        /*
         * Disabled at construction and never enabled. `SLUG_PATTERN` and `SHORT_DESCRIPTION_MAX`
         * still hang off them so that if a backend ever grows these fields, enabling the control is
         * the whole change — the rules the design implies are already written down.
         */
        legalName: this.unbacked<string>(''),
        slug: this.unbacked<string>('', [Validators.pattern(SLUG_PATTERN)]),
        category: this.unbacked<string>(''),
        timezone: this.unbacked<string>(''),
        taxNumber: this.unbacked<string>(''),
        shortDescription: this.unbacked<string>('', [Validators.maxLength(SHORT_DESCRIPTION_MAX)]),
        published: this.unbacked<boolean>(false),
        maintenanceMode: this.unbacked<boolean>(false),
      },
      {validators: [defaultLanguageIsSupported]},
    );
  }

  /**
   * A control for a field the platform does not store.
   *
   * TODO(lessons.md): store identity fields with no backend. See lessons.md, "Store management —
   * six designed store fields do not exist" and "Store management — a store has no published or
   * maintenance state".
   */
  private unbacked<T extends string | boolean>(value: T, validators: ValidatorFn[] = []): FormControl<T> {
    return this.fb.control<T>({value, disabled: true}, validators);
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

/**
 * A social profile link.
 *
 * The scheme is optional but allowed, because both shapes are real: the fixture this form was built
 * against held `instagram.com/acme`, and every store on the platform holds
 * `https://instagram.com/acme`. The stricter pattern rejected the stored values outright, which made
 * *Save changes* impossible on a store that had ever set a link — the section was unusable before a
 * character was typed. Whatever is entered is stored verbatim; the console does not rewrite it.
 */
export const SOCIAL_URL_PATTERN = /^(https?:\/\/)?[^\s/]+\.[^\s]+$/;

/**
 * A phone number that could be dialled.
 *
 * `Validators.pattern` alone cannot express "at least six digits, however they are grouped", and
 * that is the check worth having — `(0) - .` matches any shape rule and is not a number. Runs the
 * shape test first so the two errors stay distinguishable, and passes an empty value through:
 * whether the field is mandatory is `Validators.required`'s business, not this one's.
 */
export function phoneNumber(control: AbstractControl): ValidationErrors | null {
  const value = String(control.value ?? '').trim();
  if (!value) {
    return null;
  }
  if (!PHONE_PATTERN.test(value)) {
    return {phone: true};
  }
  const digits = value.replace(/\D/g, '').length;
  return digits < PHONE_MIN_DIGITS ? {phone: true} : null;
}

/**
 * The default language has to be one the store actually supports.
 *
 * A group validator rather than a validator on `language`, because it is the pair that is wrong,
 * not either control on its own — and unticking a supported language has to re-run it, which a
 * validator hanging off `language` would not. seller-ui's `defaultLanguageNotInSupported` said the
 * same thing; the error is named for the field the operator should look at.
 */
export function defaultLanguageIsSupported(group: AbstractControl): ValidationErrors | null {
  const language = group.get('language')?.value as string | undefined;
  const supported = group.get('supportedLanguages')?.value as readonly string[] | undefined;

  if (!language || !supported || supported.length === 0) {
    return null;
  }
  return supported.includes(language) ? null : {defaultLanguageNotSupported: true};
}

/**
 * The section a save posts, for the api service's patch.
 *
 * `value` rather than `getRawValue()`, and the difference is the whole point: `getRawValue()`
 * includes disabled controls, so the fields the platform cannot store were reaching the patch —
 * empty, but present, and one careless mapping away from being sent. Reading `value` makes
 * "disabled" mean "cannot be submitted" at the seam rather than by convention downstream.
 *
 * The only disabled controls on this page are `UNBACKED_DETAIL_FIELDS`. If a section ever disables
 * a control it does want submitted, it must re-enable it before saving rather than change this.
 */
export function sectionValueOf(form: SettingsForm, key: SettingsSectionKey): Record<string, unknown> {
  return (form.controls[key] as FormGroup).value as Record<string, unknown>;
}
