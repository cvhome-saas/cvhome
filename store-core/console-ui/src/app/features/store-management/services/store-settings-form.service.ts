import {Injectable, inject, signal} from '@angular/core';
import {
  FormControl,
  FormGroup,
  NonNullableFormBuilder,
  Validators,
  type AbstractControl,
  type AsyncValidatorFn,
  type ValidationErrors,
  type ValidatorFn,
} from '@angular/forms';
import {Observable, catchError, map, of, switchMap, timer} from 'rxjs';import {defaultLanguageIsSupported} from '@shared/validators/default-language-is-supported';
import {phoneNumber} from '@shared/validators/phone-number';


import {DnsCheckService} from '@api/dns/dns-check.service';
import {
  CUSTOM_DOMAIN_PATTERN,
  SHORT_DESCRIPTION_MAX,
  SLUG_PATTERN,
  UNBACKED_DETAIL_FIELDS,
  bareHostname,
  type SettingsSectionKey,
  type StoreSettings,
} from '@models/store-settings';

/** How long the field waits after the last keystroke before asking a resolver anything. */
const DNS_DEBOUNCE_MS = 600;

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
/**
 * A provider's own credentials.
 *
 * The secret is here, editable, because it comes back from the server — `SocialLoginConfigMapper`
 * decrypts before serialising. The mockup's write-only "replace" flow described a different API.
 */
export type LoginProviderForm = FormGroup<{
  enabled: FormControl<boolean>;
  appId: FormControl<string>;
  appSecret: FormControl<string>;
  /**
   * Whether a secret is already stored. Not editable — it mirrors what the read said, and it is what
   * lets an empty `appSecret` mean "keep the stored one" rather than "there is none".
   */
  hasAppSecret: FormControl<boolean>;
}>;

/** Same again for a gateway: all three fields round-trip, so all three are editable. */
export type GatewayForm = FormGroup<{
  enabled: FormControl<boolean>;
  apiKey: FormControl<string>;
  secretKey: FormControl<string>;
  webhookSecret: FormControl<string>;
}>;

/*
 * The keyed groups. How many keys each has is the server's answer rather than a constant, so
 * they are indexed rather than spelled out — `reset` brings them in line with what arrived.
 */
export type SocialLoginForm = FormGroup<Record<string, LoginProviderForm>>;
export type PaymentsForm = FormGroup<Record<string, GatewayForm>>;

export interface SettingsForms {
  domain: DomainForm;
  details: DetailsForm;
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
  private readonly dns = inject(DnsCheckService);

  /**
   * The hostname a custom domain has to CNAME to, from the last load.
   *
   * The async validator needs it and the form is built before any data has arrived, so it cannot be
   * closed over at construction — `reset` fills it in. `null` means the pod lookup was refused, and
   * the validator stands down rather than checking against nothing.
   */
  private readonly podTarget = signal<string | null>(null);

  /**
   * Whether the last lookup could not be made at all.
   *
   * Kept beside the control rather than as a validation error, because it is not one: a resolver we
   * could not reach says nothing about the operator's DNS. Making it an error would block the field
   * outright on any network that filters `dns.google` — the check would go from a safeguard to a
   * lock with no way past it. The section shows it as a warning instead.
   */
  readonly dnsCheckUnavailable = signal(false);

  /*
   * The containers are constructed rather than built through `fb`: they hold groups, not raw
   * values, so the builder's non-nullable inference has nothing to add and only obscures the
   * declared shape. Leaves still go through `fb`, which is what makes them non-nullable.
   */
  create(): SettingsForm {
    return new FormGroup<SettingsForms>({
      /*
       * Empty until a store loads. The languages a storefront is published in are the *store's*
       * supported set, which is data, not a constant — this used to be seeded from the console's
       * own en/ar locale list, which is a different list serving a different purpose. `reset` fills
       * it in, the same way the provider-keyed groups are filled in.
       */
      domain: this.fb.group({
        customDomain: this.fb.control('', {
          validators: [Validators.pattern(CUSTOM_DOMAIN_PATTERN)],
          asyncValidators: [this.dnsPointsToPod()],
        }),
      }),
      details: this.details(),
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
    /*
     * Emptied, not prefilled. The field adds a domain — the router has no update, only allocate and
     * remove — so seeding it with an existing one invited the operator to "edit" a hostname and get a
     * second allocation instead. The allocated domains are a list above it now.
     */
    this.podTarget.set(settings.podTarget);
    this.dnsCheckUnavailable.set(false);
    form.controls.domain.reset({customDomain: ''});

    const payments = form.controls.payments;
    this.syncKeys(
      payments,
      settings.payments.map((gateway) => gateway.paymentType),
      () =>
        this.fb.group({
          enabled: this.fb.control(false),
          apiKey: this.fb.control(''),
          secretKey: this.fb.control(''),
          webhookSecret: this.fb.control(''),
        }),
    );
    for (const gateway of settings.payments) {
      payments.controls[gateway.paymentType].reset({
        enabled: gateway.enabled,
        apiKey: gateway.credentials?.apiKey ?? '',
        secretKey: gateway.credentials?.secretKey ?? '',
        webhookSecret: gateway.credentials?.webhookSecret ?? '',
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

  /**
   * Refuses a custom domain that does not already point at this store's pod.
   *
   * seller-ui had this and it is the reason the old screen was safe to use: a domain allocated
   * before its CNAME exists resolves to nothing, the storefront is unreachable on it, and the seller
   * has no way to tell whether they mistyped the record, mistyped the domain, or are simply waiting.
   * The check moves that discovery to the moment of typing.
   *
   * How it differs from the original, which was subtly broken: seller-ui's version subscribed to
   * `control.valueChanges` *inside* the validator and took `first()`, so the run for the current
   * value never completed until the value changed again. Angular already gives an async validator
   * the cancellation it needs — a new run unsubscribes the previous one — so debouncing is just a
   * `timer` at the head of the stream, and the value under test is the control's own.
   *
   * Three cases deliberately do not block:
   *
   * - **No pod target.** The lookup that says where this store lives is refused for a suspended or
   *   archived store. Nothing to compare against is not evidence of a wrong record.
   * - **The resolver could not be reached.** Recorded on `dnsCheckUnavailable` and shown as a
   *   warning; see the field's own note.
   * - **An empty or malformed value.** `Validators.required` and the pattern own those, and Angular
   *   does not run async validators while a sync one is failing anyway.
   */
  private dnsPointsToPod(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      const domain = bareHostname(String(control.value ?? ''));
      const target = this.podTarget();

      this.dnsCheckUnavailable.set(false);
      if (!domain || !target) {
        return of(null);
      }

      return timer(DNS_DEBOUNCE_MS).pipe(
        switchMap(() => this.dns.checkCname(domain, target)),
        map((outcome) => (outcome === 'points-here' ? null : {dnsNotPointing: {outcome, target}})),
        catchError(() => {
          this.dnsCheckUnavailable.set(true);
          return of(null);
        }),
      );
    };
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
   *
   * `make` is handed the key, because a login provider's controls depend on which provider's row it
   * is: only the ones that carry credentials get a secret field.
   */
  private syncKeys<T extends FormGroup | FormControl>(
    group: FormGroup<Record<string, T>>,
    keys: readonly string[],
    make: (key: string) => T,
  ): void {
    for (const key of keys) {
      if (!group.contains(key)) {
        group.addControl(key, make(key));
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
 * Credentials a provider needs once it is switched on — enforced only against changes.
 *
 * A group validator rather than `Validators.required` per field, because the rule is about the
 * pair: turning the switch on is what makes the credentials matter, and a validator on `appId`
 * would not re-run when `enabled` changed.
 *
 * The `dirty` check is the important half. Stores in the wild already have providers enabled with
 * an empty app id — a credential written before encryption reads back as nothing (see lessons.md),
 * and that state loads straight into this form. Blocking on it unconditionally would mean one
 * unreadable row made the whole section unsavable, so an operator could not fix their Google secret
 * because of a Facebook row they never touched. Untouched, it reports; touched, it refuses. The
 * operator cannot create the problem and is not held hostage by it either.
 */
export function credentialsWhenEnabled(fields: readonly string[]): ValidatorFn {
  return (group: AbstractControl): ValidationErrors | null => {
    if (!group.get('enabled')?.value) {
      return null;
    }
    const missing = fields.filter((field) => !String(group.get(field)?.value ?? '').trim());
    if (missing.length === 0) {
      return null;
    }
    return group.dirty ? {credentialsWhenEnabled: {fields: missing}} : null;
  };
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
