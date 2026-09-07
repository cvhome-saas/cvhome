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

/** A menu row's height and the list's padding, in px — what the flip decision is estimated from. */
const ITEM_HEIGHT = 34;
const LIST_PADDING = 12;
/** The gap between trigger and list, matching `.popover`'s `.5rem`. */
const GAP = 8;

/**
 * The list's fixed position, as insets only — one vertical and one horizontal edge, the others null.
 * Deliberately no `transform`: `.popover`'s entrance keyframe animates `transform`, and a placement
 * that relied on it was overridden for the animation's duration, so the list visibly jumped in from
 * the wrong side.
 */
interface Placement {
  readonly top: number | null;
  readonly bottom: number | null;
  readonly left: number | null;
  readonly right: number | null;
}

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
 *
 * **The list is placed `fixed`, from the trigger's rectangle, not absolutely under it.** A row menu
 * lives inside a data table whose `.table-scroll` clips overflow, so the last row's menu opened
 * into the clipped zone and was invisible; a viewport-anchored list escapes every ancestor's
 * overflow, flips above the trigger when there is no room below, and is closed by any scroll —
 * cheaper and more honest than following the trigger around.
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
        <div
          class="popover menu"
          role="menu"
          tabindex="-1"
          [style.top.px]="placement().top"
          [style.bottom.px]="placement().bottom"
          [style.left.px]="placement().left"
          [style.right.px]="placement().right"
          (keydown)="onMenuKey($event)"
        >
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
  /** Where the fixed list goes, computed from the trigger the moment it opens. */
  protected readonly placement = signal<Placement>({top: 0, bottom: null, left: null, right: 0});
  private readonly closeOnScroll = (): void => this.close();
  protected readonly enabledCount = computed(
    () => this.actions().filter((a) => !a.disabled).length,
  );

  toggle(): void {
    if (this.open()) {
      this.close();
      return;
    }
    this.place();
    this.open.set(true);
    queueMicrotask(() => this.focusItem(0));
  }

  close(): void {
    if (this.open()) {
      document.removeEventListener('scroll', this.closeOnScroll, true);
      window.removeEventListener('resize', this.closeOnScroll);
    }
    this.open.set(false);
  }

  /**
   * The list's end edge sits on the trigger's end edge (the menu opens inward, whichever the writing
   * direction), below the trigger when it fits and above it when it would run off the viewport — all
   * as viewport insets, so the list needs no measuring and no transform. The height is estimated from
   * the entries rather than measured, because the list does not exist yet.
   */
  private place(): void {
    const trigger = this.trigger();
    if (!trigger) {
      return;
    }
    const rect = trigger.getBoundingClientRect();
    const estimated = this.actions().length * ITEM_HEIGHT + LIST_PADDING;
    const rtl = getComputedStyle(trigger).direction === 'rtl';
    const below = rect.bottom + GAP + estimated <= window.innerHeight - GAP;
    this.placement.set({
      top: below ? rect.bottom + GAP : null,
      bottom: below ? null : window.innerHeight - rect.top + GAP,
      left: rtl ? rect.left : null,
      right: rtl ? null : window.innerWidth - rect.right,
    });
    document.addEventListener('scroll', this.closeOnScroll, true);
    window.addEventListener('resize', this.closeOnScroll);
  }

  protected pick(action: MenuAction): void {
    this.close();
    this.picked.emit(action);
  }

  protected onTriggerKey(event: KeyboardEvent): void {
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault();
      if (!this.open()) {
        this.place();
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
