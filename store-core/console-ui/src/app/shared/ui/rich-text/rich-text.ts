import {isPlatformBrowser} from '@angular/common';
import {
  Component,
  ElementRef,
  Injector,
  PLATFORM_ID,
  ViewEncapsulation,
  afterNextRender,
  computed,
  forwardRef,
  inject,
  input,
  model,
  signal,
  viewChild,
  viewChildren,
} from '@angular/core';
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from '@angular/forms';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import type {IconName} from '@shared/ui/icon/icon-paths';
import {
  isEffectivelyEmpty,
  joinWrapper,
  safeHref,
  sanitizeHtml,
  splitWrapper,
} from './rich-text-html';

/** How long after the last keystroke the value is published. See `flush`. */
const TYPING_DEBOUNCE_MS = 300;

/** One toolbar button. `command` is what `runCommand` issues; `mark` is what lights it up. */
interface Tool {
  readonly key: string;
  readonly icon: IconName;
  readonly labelKey: string;
  readonly shortcutKey: string;
}

const TOOLS: readonly Tool[] = [
  {key: 'h2', icon: 'heading2', labelKey: 'shared.richText.heading2', shortcutKey: 'shared.richText.keys.heading2'},
  {key: 'h3', icon: 'heading3', labelKey: 'shared.richText.heading3', shortcutKey: 'shared.richText.keys.heading3'},
  {key: 'bold', icon: 'bold', labelKey: 'shared.richText.bold', shortcutKey: 'shared.richText.keys.bold'},
  {key: 'italic', icon: 'italic', labelKey: 'shared.richText.italic', shortcutKey: 'shared.richText.keys.italic'},
  {key: 'bullet', icon: 'listBullet', labelKey: 'shared.richText.bulletList', shortcutKey: 'shared.richText.keys.bulletList'},
  {key: 'number', icon: 'listOrdered', labelKey: 'shared.richText.numberList', shortcutKey: 'shared.richText.keys.numberList'},
  {key: 'link', icon: 'link', labelKey: 'shared.richText.link', shortcutKey: 'shared.richText.keys.link'},
  {key: 'clear', icon: 'eraser', labelKey: 'shared.richText.clear', shortcutKey: 'shared.richText.keys.clear'},
];

/** Which formats the current selection sits inside. Drives every button's pressed state. */
interface Marks {
  readonly bold: boolean;
  readonly italic: boolean;
  readonly bullet: boolean;
  readonly number: boolean;
  readonly block: string;
  readonly link: boolean;
}

const NO_MARKS: Marks = {bold: false, italic: false, bullet: false, number: false, block: 'p', link: false};

let nextId = 0;

/**
 * A rich text field, themed like everything else in the console.
 *
 * The catalogue needs one because the data is already HTML: the seeded product descriptions are
 * `<div><h2>…</h2><p>…</p><ul><li><strong>…`, and the Arabic rows are wrapped in
 * `<div dir="rtl">`. seller-ui edited them with `ngx-quill`. A textarea shows a seller raw markup
 * and lets one careless edit destroy it.
 *
 * **Why not ngx-quill.** `quill.snow.css` hardcodes `#fff`, `#ccc` and `#444`. The design system's
 * rule is explicit — "a component that hardcodes `rgb(255 255 255 / 6%)` works in two themes and
 * breaks in the third" — and Daylight is where it would break. Overriding Quill's stylesheet into
 * three themes is more work, and more fragile, than the toolbar below.
 *
 * **`document.execCommand`, deprecated and used deliberately.** The decisive reason is the native
 * **undo stack**: mutating the DOM outside the browser's editing pipeline silently kills `Ctrl+Z`
 * inside a contenteditable, and rebuilding undo — with caret restoration and keystroke coalescing —
 * is more code than this entire component. Toggling a list across a selection that starts mid
 * paragraph is the second reason. It is obsolete but universally implemented, no engine has
 * signalled removal, and the exposure is bounded: the only thing it can do to us is emit a tag
 * outside the allow-list, and `rich-text-html.ts` runs over its output anyway.
 *
 * **The wrapper is held outside the editable.** `formatBlock` and `insertOrderedList` hoist nodes
 * out of a single wrapping block, or duplicate it — so `<div dir="rtl">` would not survive being
 * edited inside. `writeValue` splits it off, the editable holds only the content, and `read()` puts
 * it back byte for byte.
 *
 * **`ViewEncapsulation.None`, every selector prefixed `.rich-text`.** The editor's content is
 * written imperatively, so Angular never stamps its encapsulation attribute onto those nodes and
 * scoped rules for `h2`, `ul` and `a` would not apply to them.
 */
@Component({
  selector: 'app-rich-text',
  imports: [Icon, TranslocoDirective],
  templateUrl: './rich-text.html',
  styleUrl: './rich-text.css',
  encapsulation: ViewEncapsulation.None,
  host: {
    /*
     * The id belongs to the control this draws, not to the host.
     *
     * A static or bound `id` on a component element lands in the DOM *as well as* matching the
     * `id` input, so the host and the inner control ended up carrying the same id — invalid HTML,
     * and `<label for>` then resolves to the host, which is not a labelable element, so the
     * association silently does not happen. Found by probing where the id actually went; four of
     * these six components had shipped with it.
     */
    '[attr.id]': 'null',
    class: 'rich-text',
    '[class.rich-text-disabled]': 'isDisabled()',
    '[class.rich-text-invalid]': 'invalid()',
    '(document:selectionchange)': 'syncMarks()',
  },
  providers: [{provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => RichText), multi: true}],
})
export class RichText implements ControlValueAccessor {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly injector = inject(Injector);
  private readonly transloco = inject(TranslocoService);

  /** The HTML document, exactly as it goes on the wire. `''` means nothing has been written. */
  readonly value = model<string>('');
  /** Mirrors the native `id`, so an existing `<label for>` keeps working. */
  readonly id = input<string | null>(null);
  readonly ariaLabel = input<string | null>(null);
  readonly placeholder = input<string | null>(null);
  readonly disabled = input(false);
  /** The consumer's `control.invalid && touched`. Draws the error frame; does not block typing. */
  readonly invalid = input(false);
  /**
   * The base direction of the **content**, which is not the console's.
   *
   * `'auto'` lets the text decide. A consumer editing a known language passes it explicitly, which
   * is how a freshly written Arabic description acquires its `<div dir="rtl">` wrapper.
   */
  readonly contentDir = input<'auto' | 'ltr' | 'rtl'>('auto');
  /** Whether the HTML source view is offered. */
  readonly allowSource = input(true);

  protected readonly tools = TOOLS;
  protected readonly editorId = `rich-text-${nextId++}`;
  protected readonly marks = signal<Marks>(NO_MARKS);
  protected readonly sourceMode = signal(false);
  protected readonly linkOpen = signal(false);
  protected readonly linkHref = signal('');
  protected readonly announcement = signal('');
  protected readonly isEmpty = signal(true);
  protected readonly toolbarIndex = signal(0);

  private readonly editable = viewChild<ElementRef<HTMLElement>>('editable');
  private readonly toolButtons = viewChildren<ElementRef<HTMLButtonElement>>('tool');
  private readonly linkInput = viewChild<ElementRef<HTMLInputElement>>('linkInput');

  private readonly isBrowser = isPlatformBrowser(this.platformId);
  private readonly formDisabled = signal(false);
  private readonly focused = signal(false);

  /**
   * The wrapper's direction, held out of the editable. See the class note.
   *
   * A signal, not a field: `editableDir` is a computed over it, and a plain field left the editable
   * on `dir="auto"` after loading an Arabic document — which guesses right from the first strong
   * character and guesses wrong for a description that opens with a Latin brand name or a digit.
   */
  private readonly wrapperDir = signal<string | null>(null);
  /** The document as the server last sent it, for the untouched round trip. */
  private pristine = '';
  /** The last value this component published, so its own echo does not reset the caret. */
  private lastEmitted = '';
  private typingTimer: ReturnType<typeof setTimeout> | null = null;
  private savedRange: Range | null = null;
  private commandDefaultsApplied = false;

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  protected readonly isDisabled = computed(() => this.disabled() || this.formDisabled());

  /** The direction the editable renders in — the wrapper's if it has one, else the consumer's. */
  protected readonly editableDir = computed(() => this.wrapperDir() ?? this.contentDir());

  protected readonly resolvedLabel = computed(() => {
    this.transloco.activeLang();
    return this.ariaLabel() ?? this.transloco.translate('shared.richText.defaultLabel');
  });

  /* --------------------------------------------------------------------------- CVA ---- */

  writeValue(value: string | null): void {
    const incoming = value ?? '';
    this.pristine = incoming;

    // Our own value coming back around through the form. Writing it again would reset the caret.
    if (this.lastEmitted === incoming) {
      this.value.set(incoming);
      return;
    }
    this.lastEmitted = incoming;
    this.value.set(incoming);

    if (!this.isBrowser) {
      return;
    }
    // Never rewrite the DOM under a caret. The operator is mid-word; the form is not the authority
    // on what they have typed.
    if (this.focused()) {
      return;
    }
    this.render(incoming);
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.formDisabled.set(isDisabled);
  }

  /* ------------------------------------------------------------------------ editing ---- */

  /**
   * Loads a document into the editable.
   *
   * Only ever called when the editable does not hold focus, and only in the browser — the server
   * renders an empty region and `afterNextRender` never runs there.
   */
  private render(html: string): void {
    const {wrapperDir, inner} = splitWrapper(html);
    this.wrapperDir.set(wrapperDir);
    const clean = sanitizeHtml(inner);
    this.isEmpty.set(isEffectivelyEmpty(clean));

    const write = () => {
      const element = this.editable()?.nativeElement;
      if (element && element.innerHTML !== clean) {
        element.innerHTML = clean;
      }
    };
    // `afterNextRender` rather than a microtask: this app runs zone change detection with
    // `eventCoalescing`, so a microtask can land before the view exists. Same reason DatePicker
    // gives for its own post-render work.
    if (this.editable()) {
      write();
    } else {
      afterNextRender(write, {injector: this.injector});
    }
  }

  /**
   * What the editable currently holds, as a document ready for the wire.
   *
   * The `pristine` short-circuit matters more than it looks: browsers normalise whitespace and
   * attribute order on parse, so a document nobody touched can come back subtly different. If it
   * sanitises to the same thing it went in as, the original goes back out.
   */
  private read(): string {
    const element = this.editable()?.nativeElement;
    if (!element) {
      return this.value();
    }
    const inner = sanitizeHtml(element.innerHTML);
    if (isEffectivelyEmpty(inner)) {
      return '';
    }
    const dir = this.wrapperDir() ?? (this.contentDir() === 'rtl' ? 'rtl' : null);
    const html = joinWrapper(inner, dir);

    /*
     * Compared inside the wrapper, not around it. Sanitising drops whitespace-only nodes between
     * top-level blocks but leaves them alone one level down, so comparing the two whole documents
     * made the pristine check fail on every wrapped document with a newline in it — which is every
     * Arabic row the platform seeded.
     */
    const before = splitWrapper(this.pristine);
    const unchanged = before.wrapperDir === dir && sanitizeHtml(before.inner) === inner;
    return unchanged ? this.pristine : html;
  }

  /**
   * Publishes the current document, if it has actually changed.
   *
   * Idempotent on purpose. `clearServerErrorsOnChange` calls `setErrors` on every `valueChanges`,
   * and consumers hang counters off it, so an emission that says nothing is not free.
   */
  private flush(): void {
    if (this.typingTimer) {
      clearTimeout(this.typingTimer);
      this.typingTimer = null;
    }
    const next = this.read();
    if (next === this.lastEmitted) {
      return;
    }
    this.lastEmitted = next;
    this.value.set(next);
    this.onChange(next);
  }

  /**
   * A keystroke.
   *
   * The value is **not** published per keystroke. Each one would run a `DOMParser` pass over the
   * whole document and a full form-status recomputation up the tree. A 300ms trailing publish keeps
   * server-error clearing feeling immediate while doing that a few times a second rather than
   * fifteen.
   */
  protected onInput(): void {
    const element = this.editable()?.nativeElement;
    this.isEmpty.set(!element || isEffectivelyEmpty(element.innerHTML));
    if (this.typingTimer) {
      clearTimeout(this.typingTimer);
    }
    this.typingTimer = setTimeout(() => this.flush(), TYPING_DEBOUNCE_MS);
  }

  protected onFocus(): void {
    this.focused.set(true);
    this.ensureCommandDefaults();
    this.syncMarks();
  }

  protected onBlur(): void {
    this.focused.set(false);
    this.flush();
    this.onTouched();
    // The value the form now holds, normalised. Safe: nothing has focus, so no caret to lose.
    this.render(this.lastEmitted);
  }

  /**
   * Pasted content, reduced to the allowed tag set before it enters the document.
   *
   * `insertHTML` rather than replacing `innerHTML`, so the paste is one undo step and the caret
   * lands after it. A paste with no usable HTML falls back to its plain text.
   */
  protected onPaste(event: ClipboardEvent): void {
    event.preventDefault();
    if (this.isDisabled()) {
      return;
    }
    const html = event.clipboardData?.getData('text/html') ?? '';
    const text = event.clipboardData?.getData('text/plain') ?? '';
    const clean = html ? sanitizeHtml(html) : '';
    const payload = clean && !isEffectivelyEmpty(clean) ? clean : asParagraphs(text);
    if (!payload) {
      return;
    }
    this.runCommand(() => document.execCommand('insertHTML', false, payload));
  }

  /** A drop bypasses the paste path entirely — files and rich fragments both arrive unsanitised. */
  protected onDrop(event: DragEvent): void {
    event.preventDefault();
  }

  /* ----------------------------------------------------------------------- commands ---- */

  /**
   * Issues one editing command.
   *
   * Restores the selection first, because the keyboard path genuinely moves focus to the toolbar.
   * (The pointer path cannot: every button prevents its own `mousedown`.) Publishes immediately
   * afterwards rather than on the typing debounce — a toolbar click is a decision, not a keystroke.
   */
  private runCommand(apply: () => void): void {
    if (this.isDisabled() || !this.isBrowser) {
      return;
    }
    this.restoreSelection();
    this.ensureCommandDefaults();
    apply();
    this.syncMarks();
    this.onInput();
    this.flush();
  }

  /**
   * Pins `execCommand`'s two document-wide settings before anything else runs.
   *
   * `styleWithCSS = false` is what keeps Bold as `<strong>` rather than a `style` span the
   * sanitiser would then have to unwrap, losing the emphasis. `defaultParagraphSeparator = p`
   * stops Chrome emitting `<div>` per line.
   */
  private ensureCommandDefaults(): void {
    if (this.commandDefaultsApplied || !this.isBrowser) {
      return;
    }
    this.commandDefaultsApplied = true;
    try {
      document.execCommand('styleWithCSS', false, 'false');
      document.execCommand('defaultParagraphSeparator', false, 'p');
    } catch {
      // Both are optional refinements; the sanitiser copes either way.
    }
  }

  protected activate(tool: string): void {
    switch (tool) {
      case 'h2':
      case 'h3':
        this.toggleBlock(tool);
        break;
      case 'bold':
        this.runCommand(() => document.execCommand('bold'));
        break;
      case 'italic':
        this.runCommand(() => document.execCommand('italic'));
        break;
      case 'bullet':
        this.runCommand(() => document.execCommand('insertUnorderedList'));
        break;
      case 'number':
        this.runCommand(() => document.execCommand('insertOrderedList'));
        break;
      case 'link':
        this.openLink();
        break;
      case 'clear':
        this.clearFormatting();
        break;
    }
  }

  /** Applying the block a second time returns to a paragraph, which is how a heading is undone. */
  private toggleBlock(tag: 'h2' | 'h3'): void {
    const next = this.marks().block === tag ? 'p' : tag;
    this.runCommand(() => document.execCommand('formatBlock', false, `<${next}>`));
  }

  /**
   * Three commands in one step: strip inline formatting, return to a paragraph, and leave any list.
   *
   * `removeFormat` alone leaves a heading a heading and a list item a list item, which is not what
   * "clear formatting" means to anyone who presses it.
   */
  private clearFormatting(): void {
    this.runCommand(() => {
      document.execCommand('removeFormat');
      document.execCommand('unlink');
      if (this.marks().bullet) {
        document.execCommand('insertUnorderedList');
      }
      if (this.marks().number) {
        document.execCommand('insertOrderedList');
      }
      document.execCommand('formatBlock', false, '<p>');
    });
  }

  /**
   * Whether a tool can act on the current selection.
   *
   * `formatBlock` inside a list is a no-op in Chrome and produces a heading wrapping an `<li>` in
   * Firefox, so the two heading buttons are refused there rather than left to misbehave.
   */
  protected available(tool: string): boolean {
    if (tool !== 'h2' && tool !== 'h3') {
      return true;
    }
    return !this.marks().bullet && !this.marks().number;
  }

  protected pressed(tool: string): boolean {
    const marks = this.marks();
    switch (tool) {
      case 'h2':
      case 'h3':
        return marks.block === tool;
      case 'bold':
        return marks.bold;
      case 'italic':
        return marks.italic;
      case 'bullet':
        return marks.bullet;
      case 'number':
        return marks.number;
      case 'link':
        return marks.link;
      default:
        return false;
    }
  }

  /**
   * Re-reads what the selection sits inside.
   *
   * Bound to the document, because a selection change is not an event any element owns. It must
   * therefore bail in one step when the selection is somewhere else — `copy-fields` can mount one
   * of these per language.
   */
  protected syncMarks(): void {
    if (!this.isBrowser) {
      return;
    }
    const element = this.editable()?.nativeElement;
    const selection = document.getSelection();
    const anchor = selection?.anchorNode ?? null;
    if (!element || !anchor || !element.contains(anchor)) {
      return;
    }
    if (selection && selection.rangeCount > 0) {
      this.savedRange = selection.getRangeAt(0).cloneRange();
    }

    const block = String(safeQueryValue('formatBlock') || 'p').toLowerCase();
    const node = anchor.nodeType === Node.ELEMENT_NODE ? (anchor as Element) : anchor.parentElement;
    this.marks.set({
      bold: safeQueryState('bold'),
      italic: safeQueryState('italic'),
      bullet: safeQueryState('insertUnorderedList'),
      number: safeQueryState('insertOrderedList'),
      block: block === 'h2' || block === 'h3' ? block : 'p',
      link: node?.closest('a') !== null && node?.closest('a') !== undefined,
    });
  }

  private restoreSelection(): void {
    const element = this.editable()?.nativeElement;
    if (!element) {
      return;
    }
    element.focus();
    const selection = document.getSelection();
    if (!selection || !this.savedRange || !element.contains(this.savedRange.commonAncestorContainer)) {
      return;
    }
    selection.removeAllRanges();
    selection.addRange(this.savedRange);
  }

  /* --------------------------------------------------------------------------- link ---- */

  /**
   * Opens the link row.
   *
   * An inline row rather than `window.prompt`: prompt is unstyled, blockable by the browser, and
   * inert inside a sandboxed frame.
   */
  private openLink(): void {
    if (this.isDisabled()) {
      return;
    }
    if (this.marks().link) {
      this.runCommand(() => document.execCommand('unlink'));
      return;
    }
    this.linkHref.set('');
    this.linkOpen.set(true);
    afterNextRender(() => this.linkInput()?.nativeElement.focus(), {injector: this.injector});
  }

  protected applyLink(): void {
    const href = safeHref(this.linkHref());
    if (!href) {
      // The row stays open rather than closing on a target that would have been dropped anyway.
      this.announce('shared.richText.linkRefused');
      return;
    }
    this.linkOpen.set(false);
    this.runCommand(() => document.execCommand('createLink', false, href));
  }

  protected cancelLink(): void {
    this.linkOpen.set(false);
    this.restoreSelection();
  }

  /* ------------------------------------------------------------------------- source ---- */

  /**
   * Swaps the editable for its HTML source.
   *
   * Announced, because switching modes rewrites the editable's DOM and that is an undo barrier —
   * the browser's own history does not survive it, and an operator who has just lost `Ctrl+Z`
   * deserves to know why.
   */
  protected toggleSource(): void {
    if (this.sourceMode()) {
      this.sourceMode.set(false);
      this.render(this.value());
      this.announce('shared.richText.sourceOff');
      return;
    }
    this.flush();
    this.sourceMode.set(true);
    this.announce('shared.richText.sourceOn');
  }

  protected onSourceInput(raw: string): void {
    const {wrapperDir, inner} = splitWrapper(raw);
    this.wrapperDir.set(wrapperDir);
    const clean = sanitizeHtml(inner);
    const next = isEffectivelyEmpty(clean) ? '' : joinWrapper(clean, wrapperDir);
    if (next === this.lastEmitted) {
      return;
    }
    this.lastEmitted = next;
    this.value.set(next);
    this.onChange(next);
  }

  private announce(key: string): void {
    this.announcement.set(this.transloco.translate(key));
  }

  /* ----------------------------------------------------------------------- keyboard ---- */

  /** The editor's accelerators. Everything else, including undo, is left to the browser. */
  protected onEditorKeydown(event: KeyboardEvent): void {
    const modifier = event.metaKey || event.ctrlKey;
    if (!modifier) {
      if (event.key === 'F10' && event.altKey) {
        this.focusTool(0);
        event.preventDefault();
      }
      return;
    }

    const handled = (() => {
      if (event.altKey) {
        switch (event.key) {
          case '2':
            return this.activate('h2'), true;
          case '3':
            return this.activate('h3'), true;
          case '0':
            return this.runCommand(() => document.execCommand('formatBlock', false, '<p>')), true;
          default:
            return false;
        }
      }
      switch (event.key.toLowerCase()) {
        case 'b':
          return this.activate('bold'), true;
        case 'i':
          return this.activate('italic'), true;
        case 'k':
          return this.activate('link'), true;
        case '\\':
          return this.activate('clear'), true;
        case '*':
        case '8':
          return event.shiftKey ? (this.activate('bullet'), true) : false;
        case '&':
        case '7':
          return event.shiftKey ? (this.activate('number'), true) : false;
        default:
          return false;
      }
    })();

    if (handled) {
      event.preventDefault();
    }
  }

  /**
   * The toolbar is one tab stop.
   *
   * Arrows move the roving index **without activating** — a toolbar is not a tablist, where arrows
   * select as they go. Reversed under RTL, read from the element rather than assumed.
   */
  protected onToolbarKeydown(event: KeyboardEvent): void {
    const count = TOOLS.length;
    const forward = getComputedStyle(event.currentTarget as Element).direction === 'rtl' ? -1 : 1;

    const target = ((): number | null => {
      switch (event.key) {
        case 'ArrowRight':
          return this.toolbarIndex() + forward;
        case 'ArrowLeft':
          return this.toolbarIndex() - forward;
        case 'Home':
          return 0;
        case 'End':
          return count - 1;
        case 'Escape':
          this.restoreSelection();
          event.preventDefault();
          return null;
        default:
          return null;
      }
    })();

    if (target === null) {
      return;
    }
    event.preventDefault();
    this.focusTool(((target % count) + count) % count);
  }

  private focusTool(index: number): void {
    this.toolbarIndex.set(index);
    afterNextRender(() => this.toolButtons()[index]?.nativeElement.focus(), {injector: this.injector});
  }

  protected onLinkKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter') {
      event.preventDefault();
      this.applyLink();
    } else if (event.key === 'Escape') {
      event.preventDefault();
      this.cancelLink();
    }
  }
}

/* ------------------------------------------------------------------------- helpers ---- */

/**
 * Plain text as paragraphs, with every character escaped.
 *
 * The fallback when a paste carries no usable HTML. Escaping is not optional: the text goes through
 * `insertHTML`, so `<script>` typed into a text editor and pasted here would otherwise arrive as
 * markup. The sanitiser would catch it, but this is the cheaper gate and the one that keeps the
 * operator's literal characters literal.
 */
function asParagraphs(text: string): string {
  const escaped = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
  return escaped
    .split(/\r?\n/)
    .filter((line) => line.trim() !== '')
    .map((line) => `<p>${line}</p>`)
    .join('');
}

/** `queryCommandState` throws in some engines when there is no selection. Never branch on a throw. */
function safeQueryState(command: string): boolean {
  try {
    return document.queryCommandState(command);
  } catch {
    return false;
  }
}

function safeQueryValue(command: string): string {
  try {
    return document.queryCommandValue(command);
  } catch {
    return '';
  }
}
