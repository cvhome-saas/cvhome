import {AbstractControl, ValidationErrors} from '@angular/forms';

/**
 * The confirmation field agrees with the password.
 *
 * Checked here as well as by uaa, which also compares the two. The round trip is the point of doing
 * it locally — a mistyped confirmation is the one signup failure worth catching without asking the
 * server.
 */
export function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const repeat = group.get('repeatPassword')?.value;
  return password === repeat ? null : {passwordMismatch: true};
}
