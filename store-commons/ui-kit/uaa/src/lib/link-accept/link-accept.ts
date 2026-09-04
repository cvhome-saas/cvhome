import {Component, computed, effect, inject, input, signal} from '@angular/core';
import {type AbstractControl, FormControl, FormGroup, ReactiveFormsModule, type ValidationErrors, Validators} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {ApiErrorService, isApiError} from '@cvhome-saas/ui-kit';
import {notACommonPassword} from '@cvhome-saas/ui-kit/forms';
import {FormField, Icon, Panel, TextField} from '@cvhome-saas/ui-kit/ui';
import {PublicLinkService, type LinkKind} from '../public-link.service';
import type {AcceptedLink, LinkPreview} from '../uaa.models';

/** Where the page is in its one job: reading the link, taking a password, or finished — or unable to. */
type Phase = 'loading' | 'ready' | 'done' | 'unusable' | 'failed' | 'noToken';

/**
 * The public page behind a one-time link: an invitation and a password reset.
 *
 * One component for both because they are one flow — read the token, show whose account it is and
 * what the password must satisfy, take a password, say where to sign in — and differ only in the
 * words and in which public endpoint answers. The route's `data.kind` picks; `?token=` is the link.
 *
 * **In the kit because both consoles serve it.** uaa renders these pages on its own origin for a
 * platform administrator, and the seller console renders them on its own for a merchant — one flow,
 * one uaa behind it, and the copy lives in the kit's shared dictionaries so neither app restates it.
 * The only thing an app supplies is `brand`, the name above the card.
 *
 * **Anonymous, and outside the shell.** `AppSecurityConfig` permits the two paths and the two
 * `/api/v1/public/**` resources; nothing here calls an authenticated endpoint, and a signed-in
 * operator opening someone else's link is not a case worth designing for. A token that is missing,
 * expired, revoked or already spent all answer the same `404`, and the page says one thing for all
 * four: there is nothing an anonymous visitor could do differently in any of them except ask for a
 * new link.
 *
 * The password rules come from the preview rather than being hard-coded: the realm's minimum length
 * and class requirements are settings, and the form should refuse what uaa would refuse before the
 * round trip. uaa still has the last word, and its field errors land on the control.
 */
@Component({
  selector: 'app-link-accept',
  imports: [ReactiveFormsModule, TranslocoDirective, Panel, FormField, TextField, Icon],
  templateUrl: './link-accept.html',
  styleUrl: './link-accept.css',
})
export class LinkAccept {
  private readonly links = inject(PublicLinkService);
  private readonly apiErrors = inject(ApiErrorService);

  /** Route data and query parameter, bound by `withComponentInputBinding`. */
  readonly kind = input.required<LinkKind>();
  readonly token = input<string>();

  /** The product name above the card. Each console names itself; empty hides the block. */
  readonly brand = input('');

  protected readonly phase = signal<Phase>('loading');
  protected readonly preview = signal<LinkPreview | null>(null);
  protected readonly accepted = signal<AcceptedLink | null>(null);
  protected readonly submitting = signal(false);

  /**
   * The mismatch is a validator on the *repeat* control rather than the kit's group-level
   * `passwordsMatch`: `app-form-field` renders only its own control's errors, and `mismatch` is a key
   * the kit's validation vocabulary already translates. The password's `valueChanges` re-runs it so a
   * corrected first field clears a stale error on the second.
   */
  protected readonly form = new FormGroup({
    password: new FormControl('', {nonNullable: true, validators: [Validators.required, notACommonPassword]}),
    repeatPassword: new FormControl('', {nonNullable: true, validators: [Validators.required, sameAsPassword]}),
  });

  /** The rules as a list the visitor can read before typing, from the realm's settings. */
  protected readonly rules = computed(() => {
    const rules = this.preview()?.password;
    if (!rules) {
      return [];
    }
    return [
      {key: 'minLength', params: {count: rules.minLength}},
      ...(rules.requireUpper ? [{key: 'upper', params: {}}] : []),
      ...(rules.requireLower ? [{key: 'lower', params: {}}] : []),
      ...(rules.requireDigit ? [{key: 'digit', params: {}}] : []),
      ...(rules.requireSpecial ? [{key: 'special', params: {}}] : []),
    ];
  });

  constructor() {
    this.form.controls.password.valueChanges.subscribe(() => this.form.controls.repeatPassword.updateValueAndValidity());
    effect(() => {
      const token = this.token();
      const kind = this.kind();
      if (!token) {
        this.phase.set('noToken');
        return;
      }
      this.links.preview(kind, token).subscribe({
        next: (preview) => {
          this.preview.set(preview);
          this.form.controls.password.addValidators(Validators.minLength(preview.password.minLength));
          this.form.controls.password.updateValueAndValidity();
          this.phase.set('ready');
        },
        error: (failure: unknown) => this.phase.set(isApiError(failure) && failure.status === 404 ? 'unusable' : 'failed'),
      });
    });
  }

  protected submit(): void {
    const token = this.token();
    if (!token || this.form.invalid || this.submitting()) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.links.accept(this.kind(), token, this.form.controls.password.value).subscribe({
      next: (accepted) => {
        this.submitting.set(false);
        this.accepted.set(accepted);
        this.phase.set('done');
      },
      error: (failure: unknown) => {
        this.submitting.set(false);
        if (isApiError(failure) && failure.status === 404) {
          this.phase.set('unusable');
          return;
        }
        this.apiErrors.applyToForm(failure, this.form);
        this.apiErrors.notify(failure);
      },
    });
  }

  protected when(value: string | null | undefined): string {
    return value ? new Date(value).toLocaleString() : '';
  }
}

/** The repeat field agrees with its sibling `password`. `mismatch` is what `shared.validation.mismatch` names. */
function sameAsPassword(control: AbstractControl): ValidationErrors | null {
  const password = control.parent?.get('password')?.value;
  return password === undefined || password === control.value ? null : {mismatch: true};
}
