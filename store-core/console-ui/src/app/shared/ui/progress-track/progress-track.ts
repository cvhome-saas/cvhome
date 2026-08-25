import {Component, computed, input} from '@angular/core';

/**
 * How far along something is.
 *
 * **The fill has a colour of its own.** It used to be `currentColor` with no default, so a track
 * only looked filled where its caller happened to set one — and the product form's readiness bar
 * did not, so it inherited the panel's muted grey and a product at 100% looked exactly like a
 * product at 0%. A progress bar whose full state is indistinguishable from its empty one is worse
 * than no bar: it reads as decoration. `tone` is how a caller asks for a different hue now, rather
 * than by colouring the host and hoping.
 *
 * **It announces itself.** It was a pair of anonymous `<span>`s, so the one number on the panel
 * that says how close a product is to publishable was invisible to a screen reader.
 */
@Component({
  selector: 'app-progress-track',
  template: `<span class="fill" [style.inline-size.%]="clamped()"></span>`,
  styles: `
    :host {
      display: block;
      overflow: hidden;
      block-size: 0.45rem;
      border-radius: var(--radius-full);
      background: var(--track);
      /* Overridable per use: the tone input sets it, and a caller may still set it directly. */
      --progress-fill: var(--primary);
    }

    .fill {
      display: block;
      block-size: 100%;
      border-radius: inherit;
      background: var(--progress-fill);
      transition: inline-size var(--default-transition-duration) ease;
    }

    /* Complete is its own state, not merely the widest one. See the class note. */
    :host(.complete) {
      --progress-fill: var(--primary-emphasis);
    }

    :host(.tone-amber) { --progress-fill: var(--chart-4-foreground); }
    :host(.tone-red) { --progress-fill: var(--chart-5-foreground); }

    @media (prefers-reduced-motion: reduce) {
      .fill {
        transition: none;
      }
    }
  `,
  host: {
    role: 'progressbar',
    '[attr.aria-valuenow]': 'clamped()',
    'aria-valuemin': '0',
    'aria-valuemax': '100',
    '[attr.aria-label]': 'label()',
    '[class.complete]': 'clamped() >= 100',
    '[class.tone-amber]': "tone() === 'amber'",
    '[class.tone-red]': "tone() === 'red'",
  },
})
export class ProgressTrack {
  readonly value = input.required<number>();
  /** What is being measured. Without it the bar is an unnamed progressbar to a reader. */
  readonly label = input<string | null>(null);
  readonly tone = input<'primary' | 'amber' | 'red'>('primary');

  /** A percentage, kept inside its own bounds — a computed figure can overshoot. */
  protected readonly clamped = computed(() => Math.max(0, Math.min(100, Math.round(this.value()))));
}
