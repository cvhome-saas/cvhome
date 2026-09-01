import {
  Component,
  ElementRef,
  HostListener,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';

import {Icon} from '../icon/icon';
import type {IconName} from '../icon/icon-paths';

/** One entry of an action menu. `danger` draws it in the destructive hue; `disabled` keeps it listed but inert. */
export interface MenuAction {
  readonly key: string;
  readonly label: string;
  readonly icon?: IconName;
  readonly danger?: boolean;
  readonly disabled?: boolean;
}

/**
 * A "⋯" button that opens a list of actions — the row menu every content list uses, and the
 * publish split-button in the editors.
 *
 * Native elements, no library: a toggle button with `aria-expanded`, a `role="menu"` popover with
 * `role="menuitem"` buttons, arrow-key movement, Escape to close, closing on outside click. The
 * popover uses the global `.popover` surface, so it matches the toolbar's menus.
 */
@Component({
  selector: 'app-action-menu',
  imports: [Icon],
  template: `
    <div class="menu-root">
      <button
        #trigger
        class="icon-action trigger"
        [class.labelled]="!!label()"
        type="button"
        [attr.aria-haspopup]="'menu'"
        [attr.aria-expanded]="open()"
        [attr.aria-label]="ariaLabel()"
        [disabled]="disabled()"
        (click)="toggle()"
        (keydown)="onTriggerKey($event)"
      >
        @if (label(); as text) {
          <span>{{ text }}</span>
          <app-icon name="chevronDown" [size]="12" />
        } @else {
          <app-icon [name]="icon()" [size]="15" />
        }
      </button>
      @if (open()) {
        <div class="popover menu" role="menu" tabindex="-1" (keydown)="onMenuKey($event)">
          @for (action of actions(); track action.key; let i = $index) {
            <button
              class="menu-item"
              [class.danger]="action.danger"
              type="button"
              role="menuitem"
              [attr.data-index]="i"
              [disabled]="action.disabled"
              (click)="pick(action)"
            >
              @if (action.icon; as name) {
                <app-icon [name]="name" [size]="14" />
              }
              <span>{{ action.label }}</span>
            </button>
          }
        </div>
      }
    </div>
  `,
  styleUrl: './action-menu.css',
})
export class ActionMenu {
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  readonly actions = input.required<readonly MenuAction[]>();
  /** Accessible name of the trigger; required because the default trigger is an icon. */
  readonly ariaLabel = input.required<string>();
  /** A text label turns the trigger into a split-style button with a chevron. */
  readonly label = input<string | null>(null);
  readonly icon = input<IconName>('ellipsisV');
  readonly disabled = input(false);

  readonly picked = output<MenuAction>();

  protected readonly open = signal(false);
  protected readonly enabledCount = computed(
    () => this.actions().filter((a) => !a.disabled).length,
  );

  toggle(): void {
    if (this.open()) {
      this.close();
      return;
    }
    this.open.set(true);
    queueMicrotask(() => this.focusItem(0));
  }

  close(): void {
    this.open.set(false);
  }

  protected pick(action: MenuAction): void {
    this.close();
    this.picked.emit(action);
  }

  protected onTriggerKey(event: KeyboardEvent): void {
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault();
      if (!this.open()) {
        this.open.set(true);
      }
      queueMicrotask(() =>
        this.focusItem(event.key === 'ArrowDown' ? 0 : this.actions().length - 1),
      );
    }
  }

  protected onMenuKey(event: KeyboardEvent): void {
    const items = this.items();
    const current = items.indexOf(document.activeElement as HTMLButtonElement);
    if (event.key === 'Escape') {
      event.preventDefault();
      this.close();
      this.trigger()?.focus();
    } else if (event.key === 'ArrowDown') {
      event.preventDefault();
      this.focusItem((current + 1) % items.length);
    } else if (event.key === 'ArrowUp') {
      event.preventDefault();
      this.focusItem((current - 1 + items.length) % items.length);
    } else if (event.key === 'Home') {
      event.preventDefault();
      this.focusItem(0);
    } else if (event.key === 'End') {
      event.preventDefault();
      this.focusItem(items.length - 1);
    } else if (event.key === 'Tab') {
      this.close();
    }
  }

  @HostListener('document:click', ['$event'])
  protected onDocumentClick(event: MouseEvent): void {
    if (this.open() && !this.host.nativeElement.contains(event.target as Node)) {
      this.close();
    }
  }

  private items(): HTMLButtonElement[] {
    return Array.from(
      this.host.nativeElement.querySelectorAll('.menu-item:not(:disabled)'),
    ) as HTMLButtonElement[];
  }

  private trigger(): HTMLButtonElement | null {
    return this.host.nativeElement.querySelector('.trigger') as HTMLButtonElement | null;
  }

  private focusItem(index: number): void {
    const items = this.items();
    if (!items.length) {
      return;
    }
    items[Math.max(0, Math.min(index, items.length - 1))].focus();
  }
}
