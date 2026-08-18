import {Component, input} from '@angular/core';

@Component({
  selector: 'app-form-field',
  template: `
    <!-- The control is projected in, so it becomes a descendant of this label at runtime
         and is implicitly associated. The rule cannot see through ng-content. -->
    <!-- eslint-disable-next-line @angular-eslint/template/label-has-associated-control -->
    <label class="form-field">
      <span>{{ label() }}</span>
      <ng-content />
    </label>
  `,
  // Previously emitted a bare `form-field` class that no stylesheet defined, and wrapped
  // the control in a <div>, so the label was not associated with its input.
  styles: `
    .form-field {
      display: block;
    }

    span {
      display: block;
      margin-bottom: .5rem;
      color: var(--secondary-foreground);
      font-size: var(--text-3xs);
      font-weight: var(--font-weight-bold);
      letter-spacing: 0.1em;
      text-transform: uppercase;
    }
  `,
})
export class FormField {
  readonly label = input.required<string>();
}
