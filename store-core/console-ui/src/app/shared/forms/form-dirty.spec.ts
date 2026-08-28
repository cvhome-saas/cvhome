import {Component, Signal, inject} from '@angular/core';
import {TestBed} from '@angular/core/testing';
import {FormBuilder, FormGroup} from '@angular/forms';

import {formDirty} from './form-dirty';

@Component({template: ''})
class Host {
  readonly form: FormGroup = inject(FormBuilder).group({name: ['Acme'], email: ['']});
  readonly dirty: Signal<boolean> = formDirty(this.form);
}

describe('formDirty', () => {
  let host: Host;

  beforeEach(() => {
    TestBed.configureTestingModule({imports: [Host]});
    host = TestBed.createComponent(Host).componentInstance;
  });

  it('starts clean', () => {
    expect(host.dirty()).toBeFalse();
  });

  it('wakes when the operator edits', () => {
    host.form.controls['name'].markAsDirty();
    host.form.controls['name'].setValue('Acme Ltd');
    expect(host.dirty()).toBeTrue();
  });

  /*
   * The reason this exists rather than three feature-local versions. `product-form.facade.ts` read
   * `valueChanges`, which never fires for `markAsPristine()` — so a saved form stayed dirty until
   * the next keystroke, and the Save button never went quiet.
   */
  it('settles when the form is reset to pristine after a save', () => {
    host.form.controls['name'].markAsDirty();
    host.form.controls['name'].setValue('Acme Ltd');
    expect(host.dirty()).toBeTrue();

    host.form.markAsPristine();
    expect(host.dirty()).toBeFalse();
  });

  it('is not woken by merely tabbing through', () => {
    host.form.controls['email'].markAsTouched();
    expect(host.dirty()).toBeFalse();
  });
});
