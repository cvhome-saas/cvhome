import {AbstractControl, ValidationErrors} from '@angular/forms';

/**
 * The two password rules the console applies on top of a length minimum.
 *
 * They exist because nothing on the server applies any — see lessons.md, "Auth — public signup validates
 * nothing". Public signup is the one endpoint anyone on the internet may call and it creates a tenant, so the
 * form is the whole gate.
 *
 * Deliberately **not** composition rules ("one upper, one digit, one symbol"). Those push people towards
 * `Password1!`, which is on the list below, and NIST has recommended against them since SP 800-63B. Length plus
 * a screen for the passwords an attacker tries first is the rule that survives contact with real users.
 */

/**
 * The passwords a credential-stuffing list opens with, filtered to those a length minimum does not already stop.
 *
 * **A floor, not a screen.** Twenty entries catch the handful that dominate every breach corpus and nothing
 * else; `Passw0rd2024` sails through. A real screen is a server-side check against a breached-password
 * corpus, which the platform has no endpoint for. Kept short on purpose so it stays honest about what it is.
 */
const COMMON_PASSWORDS: ReadonlySet<string> = new Set([
  '12345678', '123456789', '1234567890', '123123123', '11111111', '00000000',
  'password', 'password1', 'password123', 'passw0rd',
  'qwertyuiop', 'qwerty123', 'abc12345', 'iloveyou',
  'sunshine', 'princess', 'football', 'baseball',
  'welcome1', 'admin123', 'letmein1', 'monkey123',
]);

/**
 * The shortest personal token worth matching.
 *
 * Three would flag `Ann` and `Ada` inside any password containing those three letters in a row — `bandana`
 * fails for someone named Ana — which is a rule people work around by adding a character rather than by
 * choosing a better password. Four is where the match starts meaning something.
 */
const MIN_PERSONAL_TOKEN = 4;

/** The password is not one of the handful everybody tries first. */
export function notACommonPassword(control: AbstractControl): ValidationErrors | null {
  const value = control.value;
  if (typeof value !== 'string' || value.length === 0) {
    return null;
  }
  return COMMON_PASSWORDS.has(value.toLowerCase()) ? {weakPassword: true} : null;
}

/**
 * The password does not contain the name or address it protects.
 *
 * A group validator because it is the only rule here that needs three other fields. `ada.lovelace2024` is a
 * password whose owner has just typed the rest of it into the form above, and it is the single most common
 * shape of weak password that a length rule accepts.
 */
export function passwordIsNotPersonal(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  if (typeof password !== 'string' || password.length === 0) {
    return null;
  }

  const haystack = password.toLowerCase();
  return personalTokens(group).some((token) => haystack.includes(token)) ? {passwordPersonal: true} : null;
}

/** The name and address fragments a password must not contain, lowercased and long enough to mean something. */
function personalTokens(group: AbstractControl): string[] {
  const candidates = [
    group.get('firstName')?.value,
    group.get('lastName')?.value,
    // The local part only: every address at the same provider shares its domain, so `gmail` would fail
    // half the passwords on the platform for saying nothing about this account.
    localPart(group.get('emailAddress')?.value),
  ];

  return candidates
    .filter((value): value is string => typeof value === 'string')
    .map((value) => value.trim().toLowerCase())
    .filter((value) => value.length >= MIN_PERSONAL_TOKEN);
}

function localPart(email: unknown): string {
  return typeof email === 'string' ? email.split('@')[0] : '';
}
