import {Injectable, computed, effect, inject, signal, untracked} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {TranslocoService} from '@jsverse/transloco';

import {ApiErrorService, snapshot} from '@cvhome-saas/ui-kit';
import {formDirty} from '@cvhome-saas/ui-kit/forms';
import {ToastService} from '@cvhome-saas/ui-kit/ui';
import {AdminClientService, AdminSettingsService, type RealmSettings, type RotatedSecret} from '@cvhome-saas/ui-kit/uaa';

/** How the sections are grouped on the page; the `Email` section is not built (no mail sender). */
export const SETTINGS_SECTIONS = ['general', 'authentication', 'sessions', 'keys', 'danger'] as const;

export type SettingsSection = (typeof SETTINGS_SECTIONS)[number];

function num(initial: number, min: number, max = Number.MAX_SAFE_INTEGER): FormControl<number> {
  return new FormControl(initial, {nonNullable: true, validators: [Validators.required, Validators.min(min), Validators.max(max)]});
}

function bool(initial: boolean): FormControl<boolean> {
  return new FormControl(initial, {nonNullable: true});
}

/**
 * The realm's policy, as one form.
 *
 * Loaded whole and saved whole: the server takes the document with its `version`, and refuses a
 * stale one, so the page keeps the version it loaded and asks for a reload on a 409 rather than
 * overwriting a change someone else just made.
 *
 * Seconds are edited as the unit the field names (minutes, hours, days) and converted at the edge —
 * an operator thinks "15 minutes", not "900 seconds".
 */
@Injectable()
export class SettingsFacade {
  private readonly settings = inject(AdminSettingsService);
  private readonly clients = inject(AdminClientService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly toast = inject(ToastService);
  private readonly transloco = inject(TranslocoService);

  readonly busy = signal(false);
  readonly section = signal<SettingsSection>('general');

  /** The danger zone's confirmation, and what it answered: every new secret, shown once. */
  readonly rotatingAll = signal(false);
  readonly rotatedAll = signal<readonly RotatedSecret[] | null>(null);

  readonly form = new FormGroup({
    general: new FormGroup({
      displayName: new FormControl('', {nonNullable: true, validators: [Validators.required, Validators.maxLength(100)]}),
      supportEmail: new FormControl('', {nonNullable: true, validators: [Validators.email]}),
      defaultLocale: new FormControl('en', {nonNullable: true}),
      requireEmailVerification: bool(false),
    }),
    password: new FormGroup({
      minLength: num(12, 8, 128),
      requireUpper: bool(true),
      requireLower: bool(true),
      requireDigit: bool(true),
      requireSpecial: bool(false),
      historyCount: num(5, 0, 50),
      expiryDays: num(0, 0, 3650),
      rejectBreached: bool(false),
    }),
    lockout: new FormGroup({
      threshold: num(5, 1, 100),
      durationMinutes: num(15, 1, 10080),
      permanentAfter: num(5, 0, 100),
    }),
    sessions: new FormGroup({
      idleMinutes: num(30, 1, 1440),
      maxHours: num(12, 1, 720),
      rememberMeEnabled: bool(false),
      rememberMeDays: num(30, 1, 365),
      singleSessionPerUser: bool(false),
    }),
    tokens: new FormGroup({
      defaultAccessMinutes: num(15, 1, 1440),
      maxAccessMinutes: num(60, 1, 1440),
      defaultRefreshHours: num(12, 1, 8760),
      clientSecretValidityDays: num(365, 0, 3650),
      clientSecretGraceHours: num(24, 0, 720),
    }),
    keys: new FormGroup({
      rotationDays: num(90, 0, 3650),
      retireDays: num(7, 1, 365),
    }),
    auditRetentionDays: num(365, 1, 3650),
  });

  readonly dirty = formDirty(this.form);

  private readonly loaded = snapshot(
    () => ({}),
    () => this.settings.get(),
  );

  readonly isLoading = this.loaded.isLoading;
  readonly error = this.loaded.error;
  readonly reload = () => this.loaded.reload();
  readonly current = computed(() => this.loaded.value() ?? null);

  readonly localeOptions = computed(() =>
    ['en', 'ar'].map((code) => ({value: code, label: this.transloco.translate(`settings.locale.${code}`)})),
  );

  constructor() {
    // The form follows the last good load; editing in between is preserved by only resetting on a new value.
    effect(() => {
      const value = this.current();
      if (value) {
        untracked(() => this.form.reset(this.formValue(value)));
      }
    });
  }

  private formValue(s: RealmSettings) {
    return {
      general: {
        displayName: s.displayName,
        supportEmail: s.supportEmail ?? '',
        defaultLocale: s.defaultLocale,
        requireEmailVerification: s.requireEmailVerification,
      },
      password: {...s.password},
      lockout: {
        threshold: s.lockout.threshold,
        durationMinutes: Math.round(s.lockout.durationSeconds / 60),
        permanentAfter: s.lockout.permanentAfter,
      },
      sessions: {
        idleMinutes: Math.round(s.sessions.idleSeconds / 60),
        maxHours: Math.round(s.sessions.maxSeconds / 3600),
        rememberMeEnabled: s.sessions.rememberMeEnabled,
        rememberMeDays: Math.round(s.sessions.rememberMeSeconds / 86400),
        singleSessionPerUser: s.sessions.singleSessionPerUser,
      },
      tokens: {
        defaultAccessMinutes: Math.round(s.tokens.defaultAccessTokenTtlSeconds / 60),
        maxAccessMinutes: Math.round(s.tokens.maxAccessTokenTtlSeconds / 60),
        defaultRefreshHours: Math.round(s.tokens.defaultRefreshTokenTtlSeconds / 3600),
        clientSecretValidityDays: s.tokens.clientSecretValidityDays,
        clientSecretGraceHours: s.tokens.clientSecretGraceHours,
      },
      keys: {...s.keys},
      auditRetentionDays: s.auditRetentionDays,
    };
  }

  private document(base: RealmSettings): RealmSettings {
    const v = this.form.getRawValue();
    return {
      ...base,
      displayName: v.general.displayName.trim(),
      supportEmail: v.general.supportEmail.trim() || null,
      defaultLocale: v.general.defaultLocale,
      requireEmailVerification: v.general.requireEmailVerification,
      password: {...v.password},
      lockout: {
        threshold: v.lockout.threshold,
        durationSeconds: v.lockout.durationMinutes * 60,
        permanentAfter: v.lockout.permanentAfter,
      },
      sessions: {
        idleSeconds: v.sessions.idleMinutes * 60,
        maxSeconds: v.sessions.maxHours * 3600,
        rememberMeEnabled: v.sessions.rememberMeEnabled,
        rememberMeSeconds: v.sessions.rememberMeDays * 86400,
        singleSessionPerUser: v.sessions.singleSessionPerUser,
      },
      tokens: {
        defaultAccessTokenTtlSeconds: v.tokens.defaultAccessMinutes * 60,
        maxAccessTokenTtlSeconds: v.tokens.maxAccessMinutes * 60,
        defaultRefreshTokenTtlSeconds: v.tokens.defaultRefreshHours * 3600,
        clientSecretValidityDays: v.tokens.clientSecretValidityDays,
        clientSecretGraceHours: v.tokens.clientSecretGraceHours,
      },
      keys: {...v.keys},
      auditRetentionDays: v.auditRetentionDays,
    };
  }

  save(): void {
    const base = this.current();
    if (!base || this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.busy.set(true);
    this.settings.update(this.document(base)).subscribe({
      next: () => {
        this.busy.set(false);
        this.toast.success(this.transloco.translate('settings.toast.saved'));
        this.loaded.reload();
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.apiErrors.applyToForm(failure, this.form);
        this.apiErrors.notify(failure);
      },
    });
  }

  discard(): void {
    const value = this.current();
    if (value) {
      this.form.reset(this.formValue(value));
    }
  }

  /**
   * Every secret-holding client gets a new secret. Incident response: after this, every service on
   * the platform must be reconfigured inside the grace window, and the list this answers is the only
   * time the new secrets are readable.
   */
  rotateAllSecrets(): void {
    this.busy.set(true);
    this.clients.rotateAll().subscribe({
      next: (rotated) => {
        this.busy.set(false);
        this.rotatingAll.set(false);
        this.rotatedAll.set(rotated);
        this.toast.success(this.transloco.translate('settings.danger.rotatedAll', {count: rotated.length}));
      },
      error: (failure: unknown) => {
        this.busy.set(false);
        this.rotatingAll.set(false);
        this.apiErrors.notify(failure);
      },
    });
  }
}
