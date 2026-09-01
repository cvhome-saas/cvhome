import {Component, ElementRef, computed, effect, input, output, signal, viewChild} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '../icon/icon';

/*
 * The results panel's cap, from `autocomplete.css`. Used to decide which way to open before the
 * panel exists — it has no height of its own until it renders.
 */
const PANEL_HEIGHT = 240;

/** One result. `detail` is the disambiguator — a SKU beside a name that two products share. */
export interface AutocompleteOption {
  readonly id: number;
  readonly label: string;
  readonly detail?: string;
}

/**
 * A search box that proposes records the server found.
 *
 * **Not `tag-input`.** That component collects strings the operator types; this one resolves a
 * typed fragment to a record that already exists, which is a different job with a different
 * failure mode — a typo in `tag-input` becomes a tag, and a typo here has to become "no matches".
 *
 * It owns no fetching. The term goes out debounced as `search`, the consumer answers with
 * `options`, and `loading` says a request is in flight; that keeps the request, its cancellation
 * and its error handling in the facade where the rest of the page's requests live.
 *
 * The combobox contract is the standard one: the input keeps focus throughout and
 * `aria-activedescendant` moves the reading position through the list, so arrow keys browse
 * results without the operator losing their place in what they typed.
 */
@Component({
  selector: 'app-autocomplete',
  imports: [Icon, TranslocoDirective],
  template: `
    <div class="combo" *transloco="let t">
      <label class="field">
        <span class="sr-only">{{ label() }}</span>
        <app-icon name="search" [size]="15" />
        <input
          #input
          type="text"
          role="combobox"
          autocomplete="off"
          [value]="term()"
          [placeholder]="placeholder()"
          [disabled]="disabled()"
          [attr.aria-expanded]="isOpen()"
          [attr.aria-controls]="listId"
          [attr.aria-activedescendant]="activeOptionId()"
          [attr.aria-label]="label()"
          (input)="onInput($any($event.target).value)"
          (keydown)="onKeydown($event)"
          (focus)="opened.set(true)"
          (blur)="onBlur()"
        />
        @if (loading()) {
          <span class="spinner" role="status" [attr.aria-label]="t('shared.autocomplete.searching')"></span>
        }
      </label>

      <!--
        Rendered only while open, so an idle picker adds nothing to the accessibility tree — and
        so the empty state below is genuinely "we looked and found nothing", never "we have not
        looked yet".
      -->
      @if (isOpen()) {
        <!--
          Already inside the conditional, so the leave animation has a node to run on: Angular only
          plays it when Angular is the one removing the element.
        -->
        <ul
          class="options"
          [class.drop-up]="dropUp()"
          role="listbox"
          [id]="listId"
          [attr.aria-label]="label()"
          animate.enter="options-in"
          animate.leave="options-out"
        >
          @for (option of options(); track option.id) {
            <li
              class="option"
              role="option"
              [id]="listId + '-' + option.id"
              [class.active]="option.id === activeId()"
              [attr.aria-selected]="option.id === activeId()"
              (mousedown)="$event.preventDefault(); choose(option)"
              (mouseenter)="activeId.set(option.id)"
            >
              <span class="option-label">{{ option.label }}</span>
              @if (option.detail) {
                <span class="option-detail" dir="ltr">{{ option.detail }}</span>
              }
            </li>
          } @empty {
            <li class="option empty" role="presentation">
              {{ loading() ? t('shared.autocomplete.searching') : t('shared.autocomplete.noMatches') }}
            </li>
          }
        </ul>
      }
    </div>
  `,
  styleUrl: './autocomplete.css',
})
export class Autocomplete {
  readonly options = input.required<readonly AutocompleteOption[]>();
  readonly label = input.required<string>();
  readonly placeholder = input('');
  readonly loading = input(false);
  readonly disabled = input(false);
  /** How long a pause counts as "done typing". */
  readonly debounceMs = input(250);
  /** How many characters before a search is worth making. Below this the list stays shut. */
  readonly minLength = input(2);

  /**
   * A term worth searching for, already debounced.
   *
   * Not named `search`: that is a real DOM event on `<input type="search">`, and an output sharing
   * its name would fire on both the component's emission and the browser's.
   */
  readonly termChanged = output<string>();
  readonly selected = output<AutocompleteOption>();

  protected readonly term = signal('');
  protected readonly opened = signal(false);
  /**
   * Whether the results are drawn above the field.
   *
   * Without it the panel — absolutely positioned, so it displaces nothing — still extended past the
   * bottom of the document and lengthened the scrollable area, so the page visibly grew every time
   * results appeared and shrank when they went. Absolute does not mean free.
   */
  protected readonly dropUp = signal(false);
  protected readonly activeId = signal<number | null>(null);

  /** Unique per instance, so two pickers on one page do not both own `#options`. */
  protected readonly listId = `autocomplete-${nextId++}`;

  private readonly input = viewChild<ElementRef<HTMLInputElement>>('input');
  private debounce: ReturnType<typeof setTimeout> | null = null;

  constructor() {
    /*
     * The highlight follows the results rather than persisting across them: keeping the old
     * `activeId` after a new search would leave `aria-activedescendant` pointing at an element
     * that is no longer in the list, which reads as silence.
     */
    effect(() => {
      const options = this.options();
      const active = this.activeId();
      if (active !== null && !options.some((option) => option.id === active)) {
        this.activeId.set(options[0]?.id ?? null);
      }
    });
  }

  protected readonly isOpen = computed(
    () => this.opened() && !this.disabled() && this.term().trim().length >= this.minLength(),
  );

  protected readonly activeOptionId = computed(() => {
    const active = this.activeId();
    return this.isOpen() && active !== null ? `${this.listId}-${active}` : null;
  });

  /**
   * Decides which way the results open, from the field's position in the viewport.
   *
   * Same rule as `app-select`: flip only when below genuinely does not fit and above is roomier, so
   * a field in the middle of the page keeps the ordinary downward drop.
   */
  private placeList(): void {
    const field = this.input()?.nativeElement;
    if (!field) {
      return;
    }
    const rect = field.getBoundingClientRect();
    const below = window.innerHeight - rect.bottom;
    const above = rect.top;
    this.dropUp.set(below < PANEL_HEIGHT && above > below);
  }

  protected onInput(value: string): void {
    this.placeList();
    this.term.set(value);
    this.opened.set(true);
    if (this.debounce) {
      clearTimeout(this.debounce);
    }
    const trimmed = value.trim();
    if (trimmed.length < this.minLength()) {
      return;
    }
    this.debounce = setTimeout(() => this.termChanged.emit(trimmed), this.debounceMs());
  }

  protected onKeydown(event: KeyboardEvent): void {
    const options = this.options();

    switch (event.key) {
      case 'ArrowDown':
      case 'ArrowUp': {
        if (!this.isOpen() || !options.length) {
          return;
        }
        const current = options.findIndex((option) => option.id === this.activeId());
        const step = event.key === 'ArrowDown' ? 1 : -1;
        // Wrap, so the list has no dead ends — the same rule the tab track follows.
        const next = ((current + step) % options.length + options.length) % options.length;
        this.activeId.set(options[next].id);
        break;
      }
      case 'Enter': {
        const chosen = options.find((option) => option.id === this.activeId());
        if (this.isOpen() && chosen) {
          this.choose(chosen);
        } else {
          // Not `preventDefault` on a miss: inside a form, Enter should still do whatever it does.
          return;
        }
        break;
      }
      case 'Escape':
        if (!this.isOpen()) {
          return;
        }
        this.opened.set(false);
        break;
      default:
        return;
    }
    event.preventDefault();
  }

  /**
   * Closing on blur, one tick late.
   *
   * A click on an option fires `mousedown` before `blur`, and that handler already prevents the
   * default so focus never leaves — but a click on the scrollbar or a fast tab-out does not. The
   * delay is what stops the list vanishing before a pointer's `mouseup` lands.
   */
  protected onBlur(): void {
    setTimeout(() => this.opened.set(false), 120);
  }

  protected choose(option: AutocompleteOption): void {
    this.selected.emit(option);
    // Cleared, not filled with the chosen label: this picker adds to a list, so the next thing the
    // operator types is a new search rather than an edit of the last one.
    this.term.set('');
    this.opened.set(false);
    this.activeId.set(null);
    this.input()?.nativeElement.focus();
  }
}

let nextId = 0;
