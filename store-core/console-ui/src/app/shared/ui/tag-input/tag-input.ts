import {Component, computed, inject, input, model, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';

/**
 * A chip list you type into: Enter or a comma commits a tag, Backspace on an empty field
 * removes the last one, and every chip carries a labelled remove button.
 *
 * ```html
 * <app-tag-input [tags]="tags()" (tagsChange)="tags.set($event)" [label]="t('...')" />
 * ```
 */
@Component({
  selector: 'app-tag-input',
  imports: [Icon],
  template: `
    <!-- The input stretches to fill whatever the chips leave, so the whole field is a
         click target without a handler on the container. -->
    <div class="tag-field">
      @for (tag of tags(); track tag) {
        <span class="tag">
          {{ tag }}
          <button type="button" [attr.aria-label]="removeLabel(tag)" (click)="remove(tag)">
            <app-icon name="x" />
          </button>
        </span>
      }

      <input
        type="text"
        [attr.aria-label]="resolvedLabel()"
        [placeholder]="resolvedPlaceholder()"
        [value]="draft()"
        (input)="draft.set($any($event.target).value)"
        (keydown)="onKeydown($event)"
        (blur)="commit()"
      />
    </div>
  `,
  styleUrl: './tag-input.css',
})
export class TagInput {
  private readonly transloco = inject(TranslocoService);

  readonly tags = model<readonly string[]>([]);
  /** Names the field for assistive tech; the visible label is the consumer's. */
  readonly label = input<string>();
  readonly placeholder = input<string>();

  protected readonly draft = signal('');

  protected readonly resolvedLabel = computed(
    () => this.label() ?? this.transloco.translate('shared.tagInput.defaultLabel'),
  );
  protected readonly resolvedPlaceholder = computed(
    () => this.placeholder() ?? this.transloco.translate('shared.tagInput.defaultPlaceholder'),
  );

  protected removeLabel(tag: string): string {
    return this.transloco.translate('shared.tagInput.removeTag', {tag});
  }

  protected onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' || event.key === ',') {
      event.preventDefault();
      this.commit();
      return;
    }
    // Only when there is nothing to delete in the field itself.
    if (event.key === 'Backspace' && this.draft() === '' && this.tags().length > 0) {
      event.preventDefault();
      this.tags.set(this.tags().slice(0, -1));
    }
  }

  protected commit(): void {
    const value = this.draft().trim();
    this.draft.set('');
    if (value && !this.tags().includes(value)) {
      this.tags.set([...this.tags(), value]);
    }
  }

  protected remove(tag: string): void {
    this.tags.set(this.tags().filter((existing) => existing !== tag));
  }
}
