import {Component, input, model} from '@angular/core';

/**
 * The pill switch settings surfaces are built from.
 *
 * A real `<button role="switch">` rather than the mockup's styled `<div>`, so it is
 * reachable by keyboard and announces its state. With a `label` the whole row is the
 * control; without one it is just the switch, for card headers that name themselves.
 *
 * Two-way bound, so it composes with a signal or a reactive control:
 *
 * ```html
 * <app-toggle
 *   label="Store is published"
 *   [checked]="form.controls.published.value"
 *   (checkedChange)="setFlag(form.controls.published, $event)"
 * />
 * ```
 */
@Component({
  selector: 'app-toggle',
  template: `
    <button
      class="toggle"
      type="button"
      role="switch"
      [attr.aria-checked]="checked()"
      [attr.aria-label]="label() ? null : name()"
      [disabled]="disabled()"
      (click)="flip()"
    >
      <span class="track" aria-hidden="true"><span class="knob"></span></span>

      @if (label()) {
        <span class="copy">
          <strong>{{ label() }}</strong>
          @if (description()) {
            <small>{{ description() }}</small>
          }
        </span>
      }
    </button>
  `,
  styleUrl: './toggle.css',
})
export class Toggle {
  readonly checked = model(false);
  /** Names the switch beside it. Absent for switches inside a heading that already names them. */
  readonly label = input<string | null>(null);
  readonly description = input<string | null>(null);
  /** What the switch is called for assistive tech when there is no visible `label`. */
  readonly name = input<string | null>(null);
  readonly disabled = input(false);

  protected flip(): void {
    this.checked.set(!this.checked());
  }
}
