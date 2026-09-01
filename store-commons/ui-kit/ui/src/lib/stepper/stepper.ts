import {Component, input, output} from '@angular/core';

import {Icon} from '../icon/icon';

export interface StepItem {
  readonly key: string;
  readonly label: string;
  /** One line under the label: what the step is for, or why it cannot be entered yet. */
  readonly meta?: string;
  /** Every required field on this step is filled. Draws the tick. */
  readonly complete?: boolean;
  /** Not reachable yet — a step that needs a saved record, for instance. */
  readonly disabled?: boolean;
}

/**
 * The wizard rail: numbered steps, their completion, and which one is open.
 *
 * **Not `tab-switcher` with different paint.** A tablist says "these are alternative views of the
 * same thing, pick one"; a stepper says "these are stages of one task, in order, and some of them
 * are not available yet". The ARIA contracts differ accordingly — this is a `list` whose current
 * step carries `aria-current="step"`, rather than a `tablist` with a selected tab — and a tab
 * cannot express `disabled` or `complete` at all.
 *
 * Steps stay clickable once reachable, so the rail is navigation and not a one-way conveyor: an
 * operator who wants to fix the SKU on step 1 while looking at step 4 should not have to walk back
 * through the ones in between.
 */
@Component({
  selector: 'app-stepper',
  imports: [Icon],
  template: `
    <ol class="steps" [attr.aria-label]="label()">
      @for (step of steps(); track step.key; let index = $index) {
        <li class="step-slot">
          <button
            class="step"
            type="button"
            [class.current]="step.key === active()"
            [class.complete]="step.complete"
            [disabled]="step.disabled"
            [attr.aria-current]="step.key === active() ? 'step' : null"
            (click)="stepSelected.emit(step.key)"
          >
            <!--
              The number stays in the marker whether or not the step is done. An earlier draft
              swapped it for a tick, which meant a completed step announced no position at all —
              "Media" instead of "step 2, Media". Completion is a separate mark on the label.
            -->
            <span class="marker">{{ index + 1 }}</span>

            <span class="step-copy">
              <span class="step-label">
                {{ step.label }}
                @if (step.complete) {
                  <!--
                    A labelled icon rather than a bare glyph: colour and a tick are not a label,
                    and this is the one thing on the rail a screen reader could not otherwise
                    infer from the step's own copy.
                  -->
                  <app-icon class="done" name="checkCircle" [size]="13" [label]="completeLabel()" />
                }
              </span>
              @if (step.meta) {
                <span class="step-meta">{{ step.meta }}</span>
              }
            </span>
          </button>
        </li>
      }
    </ol>
  `,
  styleUrl: './stepper.css',
})
export class Stepper {
  readonly steps = input.required<readonly StepItem[]>();
  readonly active = input.required<string>();
  /** Names the rail, e.g. "Product setup steps". */
  readonly label = input.required<string>();
  /** What the tick means, in the reader's language — "Complete". */
  readonly completeLabel = input.required<string>();

  readonly stepSelected = output<string>();
}
