import {fakeAsync, tick} from '@angular/core/testing';
import {FormControl} from '@angular/forms';
import {Observable, Subject, of, throwError} from 'rxjs';

import {uniqueAsync} from './unique-async';

describe('uniqueAsync', () => {
  const run = (
    control: FormControl,
    check: (value: string) => Observable<boolean>,
    options = {},
  ) => {
    control.setAsyncValidators(uniqueAsync(check, 'taken', options));
    control.updateValueAndValidity();
  };

  it('reports a value the server already holds', fakeAsync(() => {
    const control = new FormControl('acme');
    run(control, () => of(true));
    tick(300);
    expect(control.errors).toEqual({taken: true});
  }));

  it('passes a value nobody holds', fakeAsync(() => {
    const control = new FormControl('acme');
    run(control, () => of(false));
    tick(300);
    expect(control.errors).toBeNull();
  }));

  it('asks nothing about an empty value', fakeAsync(() => {
    const check = jasmine.createSpy('check').and.returnValue(of(true));
    const control = new FormControl('');
    run(control, check);
    tick(300);
    expect(check).not.toHaveBeenCalled();
  }));

  it('asks nothing about a value the sync rules already reject', fakeAsync(() => {
    const check = jasmine.createSpy('check').and.returnValue(of(true));
    const control = new FormControl('not a code');
    run(control, check, {when: (value: string) => /^[a-z]+$/.test(value)});
    tick(300);
    expect(check).not.toHaveBeenCalled();
  }));

  it('waits for the typing to stop before spending a request', fakeAsync(() => {
    const check = jasmine.createSpy('check').and.returnValue(of(false));
    const control = new FormControl('a');
    run(control, check);

    tick(100);
    expect(check).not.toHaveBeenCalled();
    tick(200);
    expect(check).toHaveBeenCalledTimes(1);
  }));

  /*
   * The catalogue shipped with this: an existing record's code is disabled once loaded, but the
   * form is filled while it is still enabled, so the check starts and the facade disables the
   * control a tick later. `disable()` nulls the errors present at that moment and cannot null one
   * that has not arrived. Every existing category, brand, type, group and product then carried a
   * red "already taken" against a code the operator could not even edit.
   */
  it('does not report on a control that was disabled while the answer was in flight', fakeAsync(() => {
    const answer = new Subject<boolean>();
    const control = new FormControl('acme');
    control.setAsyncValidators(uniqueAsync(() => answer.asObservable()));
    control.updateValueAndValidity();

    tick(300);
    control.disable();
    answer.next(true);
    answer.complete();
    tick();

    expect(control.errors).toBeNull();
  }));

  it('leaves the field usable when the lookup itself fails', fakeAsync(() => {
    // A check that could not be made is not a failed check — the server still has the last word.
    const control = new FormControl('acme');
    control.setAsyncValidators(uniqueAsync(() => throwError(() => new Error('offline'))));
    control.updateValueAndValidity();
    tick(300);
    expect(control.errors).toBeNull();
  }));
});
