import {Component, input, output} from '@angular/core';
import {FormControl, ReactiveFormsModule, type FormArray} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import {isInsecureUri} from '@cvhome-saas/ui-kit/forms';
import {FieldError, Icon, TextField} from '@cvhome-saas/ui-kit/ui';

/**
 * A list of URIs, one row each, with an **Add** and a remove per row.
 *
 * Replaces a single comma-separated field. A redirect URI is long, contains commas legitimately in a
 * query string, and is the field an integration breaks on — one per row is how the app this console
 * replaced did it, and it was right.
 *
 * The glyph beside each row is a *status*, not a validation result: a well-formed `http://` URI to a
 * remote host passes every rule uaa has and still hands an authorization code to anything on the
 * network path. It is worth flagging where it is typed, because it is never looked at again.
 */
@Component({
  selector: 'app-uri-list',
  imports: [ReactiveFormsModule, TranslocoDirective, TextField, FieldError, Icon],
  templateUrl: './uri-list.html',
  styleUrl: './uri-list.css',
})
export class UriList {
  readonly array = input.required<FormArray<FormControl<string>>>();
  readonly label = input.required<string>();
  readonly hint = input<string | null>(null);
  /** Shown in place of the rows when there are none, and it says why there should be one. */
  readonly emptyHint = input<string | null>(null);

  readonly add = output<void>();
  readonly remove = output<number>();

  protected insecure(control: FormControl<string>): boolean {
    return isInsecureUri(control.value);
  }

  protected valid(control: FormControl<string>): boolean {
    return control.value.trim().length > 0 && control.valid;
  }
}
