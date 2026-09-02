import {DestroyRef, Signal, inject, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {AbstractControl, PristineChangeEvent, TouchedChangeEvent} from '@angular/forms';

/**
 * Whether a form has been edited since it was last reset, as a signal.
 *
 * Three features each answered this differently and none of them was quite right.
 * `store-settings.facade.ts` and `create-store.facade.ts` read `form.events`, which is correct.
 * `product-form.facade.ts` read `valueChanges`, which never fires for `markAsPristine()` — so a
 * saved form stayed dirty until the next keystroke and the Save button never went quiet.
 * `catalogue.facade.ts` counted its own `copyStamp` because it had given up on both.
 *
 * `form.events` is the only stream that carries `PristineChangeEvent`, which is what a reset emits
 * and what "the operator has unsaved work" actually means. Everything else is a proxy for it.
 *
 * `takeUntilDestroyed` needs an injection context, so call this from a field initialiser or pass a
 * `DestroyRef` explicitly.
 */
export function formDirty(form: AbstractControl, destroyRef = inject(DestroyRef)): Signal<boolean> {
  const dirty = signal(form.dirty);

  form.events.pipe(takeUntilDestroyed(destroyRef)).subscribe((event) => {
    // `TouchedChangeEvent` fires on every blur and cannot change dirtiness; skipping it keeps a
    // form the operator merely tabbed through from waking the Save bar.
    if (event instanceof TouchedChangeEvent) {
      return;
    }
    if (event instanceof PristineChangeEvent) {
      dirty.set(!event.pristine);
      return;
    }
    dirty.set(form.dirty);
  });

  return dirty.asReadonly();
}
