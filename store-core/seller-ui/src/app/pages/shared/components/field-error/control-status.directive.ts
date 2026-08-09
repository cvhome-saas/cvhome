import {computed, Directive, effect, inject} from '@angular/core';
import {toObservable, toSignal} from '@angular/core/rxjs-interop';
import {NgControl} from '@angular/forms';
import {NbInputDirective} from '@nebular/theme';
import {switchMap} from 'rxjs';
import {serverErrorOf} from 'seller-core';

/**
 * Turns a Nebular input red while its control is in error, replacing the expression every template repeats
 * by hand:
 *
 * ```html
 * [status]="f.name.invalid && (f.name.dirty || f.name.touched) ? 'danger' : 'basic'"
 * ```
 *
 * with:
 *
 * ```html
 * <input formControlName="name" nbInput ngxControlStatus>
 * ```
 *
 * It reads the control from `NgControl`, so it works with `formControlName`, `formControl` and `ngModel`
 * alike, and it needs no reference to the facade.
 */
@Directive({
  selector: '[ngxControlStatus]',
  standalone: true,
})
export class ControlStatusDirective {

  private readonly ngControl = inject(NgControl, {self: true});

  private readonly input = inject(NbInputDirective, {self: true, optional: true});

  private readonly controlEvent = toSignal(
    toObservable(computed(() => this.ngControl.control)).pipe(
      switchMap(control => control ? control.events : []),
    ),
    {equal: () => false},
  );

  private readonly danger = computed(() => {
    this.controlEvent();
    const control = this.ngControl.control;
    if (!control) {
      return false;
    }
    return serverErrorOf(control) !== null || (control.invalid && (control.dirty || control.touched));
  });

  constructor() {
    effect(() => {
      const isDanger = this.danger();
      if (this.input) {
        this.input.status = isDanger ? 'danger' : 'basic';
      }
    });
  }
}
