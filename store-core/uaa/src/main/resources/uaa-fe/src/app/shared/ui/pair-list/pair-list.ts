import {Component, input, output} from '@angular/core';
import {FormArray, FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon, TextField} from '@cvhome-saas/ui-kit/ui';

/** A repeating `{key, value}` row. Declared here because this component is what draws it. */
export type PairArray = FormArray<FormGroup<{key: FormControl<string>; value: FormControl<string>}>>;

/** An empty one, typed — declaring `new FormArray([])` inline infers `FormArray<never>`. */
export function newPairArray(): PairArray {
  return new FormArray<FormGroup<{key: FormControl<string>; value: FormControl<string>}>>([]);
}

/** One more row on the end. */
export function pairRow(key = '', value = ''): PairArray['controls'][number] {
  return new FormGroup({
    key: new FormControl(key, {nonNullable: true}),
    value: new FormControl(value, {nonNullable: true}),
  });
}

/**
 * The open key/value map both `clientSettings` and `tokenSettings` carry.
 *
 * Spring's `ClientSettings`/`TokenSettings` are `Map<String, Object>` with a handful of well-known
 * keys read off them; anything else an authorization-server extension has put there is invisible to
 * a form that only knows the well-known ones — and the previous form, which carried the map through
 * untouched, made it uneditable as well as invisible.
 *
 * Values are sent as strings. A member that arrived as a number or an object is shown as its JSON so
 * it is at least legible, and saving it back sends the text — see lessons.md, "Clients — a custom
 * setting's value is a string".
 */
@Component({
  selector: 'app-pair-list',
  imports: [ReactiveFormsModule, TranslocoDirective, TextField, Icon],
  templateUrl: './pair-list.html',
  styleUrl: './pair-list.css',
})
export class PairList {
  readonly array = input.required<PairArray>();
  readonly label = input.required<string>();
  /**
   * Keys the server will not let go of, so their remove button is disabled and says why.
   *
   * uaa's `updateUser` merges metadata — `u.getMetadata().putAll(req.metadata())` — so a key that
   * came back from the server cannot be removed through this API at all. Offering a remove button
   * that silently does nothing is worse than not offering one. See lessons.md, "Users — metadata is
   * merged, never replaced".
   */
  readonly lockedKeys = input<readonly string[]>([]);
  readonly lockedHint = input<string | null>(null);

  readonly add = output<void>();
  readonly remove = output<number>();

  protected locked(key: string): boolean {
    return this.lockedKeys().includes(key);
  }
}
