import {Component, signal} from '@angular/core';
import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {FormControl, ReactiveFormsModule, Validators} from '@angular/forms';

import {kitTranslocoTesting} from '@cvhome-saas/ui-kit/i18n';
import {RichText} from './rich-text';

/**
 * The component-level half of the editor's coverage. `rich-text-html.spec.ts` proves the allow-list
 * on its own; this proves the parts only a live DOM can answer — the control-value round trip, the
 * wrapper being held outside the editable, the caret guard, and the toolbar's single tab stop.
 *
 * Nothing here drives `execCommand`. Headless Chrome implements it, but what it emits differs by
 * engine and version, so asserting on it would be asserting on the browser. The contract this
 * component actually makes is that whatever the command emits goes through the sanitiser on the way
 * out — which is what the write-back tests below check, by putting the markup in directly.
 */
@Component({
  imports: [RichText, ReactiveFormsModule],
  template: `
    <app-rich-text
      [formControl]="control"
      [contentDir]="dir()"
      [invalid]="invalid()"
      [placeholder]="'Describe the product'"
      ariaLabel="Description"
    />
  `,
})
class Host {
  readonly control = new FormControl('', {nonNullable: true});
  readonly dir = signal<'auto' | 'ltr' | 'rtl'>('auto');
  readonly invalid = signal(false);
}

describe('RichText', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;
  let element: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Host, ...kitTranslocoTesting().imports],
      providers: [...kitTranslocoTesting().providers],
    }).compileComponents();

    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    element = fixture.nativeElement as HTMLElement;
  });

  function editable(): HTMLElement {
    return element.querySelector<HTMLElement>('.rt-editable')!;
  }

  function tools(): HTMLButtonElement[] {
    return [...element.querySelectorAll<HTMLButtonElement>('.rt-tool:not(.rt-source-toggle)')];
  }

  function sourceToggle(): HTMLButtonElement {
    return element.querySelector<HTMLButtonElement>('.rt-source-toggle')!;
  }

  /** Puts markup into the editable the way an editing command would, then publishes it. */
  function type(html: string): void {
    editable().innerHTML = html;
    editable().dispatchEvent(new Event('input'));
  }

  /* ------------------------------------------------------------------ writing in ---- */

  it('renders the document the form gave it', () => {
    host.control.setValue('<p>Fast, light, <strong>waterproof</strong>.</p>');
    fixture.detectChanges();

    expect(editable().innerHTML).toBe('<p>Fast, light, <strong>waterproof</strong>.</p>');
  });

  it('holds the direction wrapper outside the editable', () => {
    /*
     * `formatBlock` and `insertOrderedList` hoist nodes out of a single wrapping block, or duplicate
     * it. A `<div dir="rtl">` left inside would not survive being edited around.
     */
    host.control.setValue('<div dir="rtl"><p>سريع</p></div>');
    fixture.detectChanges();

    expect(editable().innerHTML).toBe('<p>سريع</p>');
    expect(editable().getAttribute('dir')).toBe('rtl');
  });

  it('strips what the allow-list refuses before it reaches the screen', () => {
    host.control.setValue('<p>Copy<script>alert(1)</script></p><span style="color:red">Loud</span>');
    fixture.detectChanges();

    expect(editable().querySelector('script')).toBeNull();
    expect(editable().querySelector('span')).toBeNull();
    // Unwrapped, not dropped: the seller's words survive their markup.
    expect(editable().textContent).toContain('Loud');
  });

  it('shows the placeholder while the document is empty in the sense that matters', () => {
    // `:empty` would be false here — after any command the editable holds `<p><br></p>`.
    type('<p><br></p>');
    fixture.detectChanges();

    expect(editable().classList).toContain('is-empty');
    expect(editable().getAttribute('data-placeholder')).toBe('Describe the product');
  });

  /* ----------------------------------------------------------------- writing out ---- */

  it('publishes on the trailing edge of a run of keystrokes', fakeAsync(() => {
    type('<p>Fas</p>');
    tick(100);
    type('<p>Fast</p>');

    // Not yet: a keystroke costs a DOMParser pass and a form-status recomputation up the tree.
    expect(host.control.value).toBe('');

    tick(300);
    expect(host.control.value).toBe('<p>Fast</p>');
  }));

  it('publishes immediately on blur rather than waiting out the timer', fakeAsync(() => {
    type('<p>Fast</p>');
    editable().dispatchEvent(new Event('blur'));

    expect(host.control.value).toBe('<p>Fast</p>');
    expect(host.control.touched).toBe(true);
    tick(300);
  }));

  it('puts the wrapper back exactly as it arrived', fakeAsync(() => {
    host.control.setValue('<div dir="rtl"><p>سريع</p></div>');
    fixture.detectChanges();

    type('<p>سريع وخفيف</p>');
    tick(300);

    expect(host.control.value).toBe('<div dir="rtl"><p>سريع وخفيف</p></div>');
  }));

  it('wraps a new document when the consumer says the language is right-to-left', fakeAsync(() => {
    host.dir.set('rtl');
    fixture.detectChanges();

    type('<p>جديد</p>');
    tick(300);

    // How a freshly written Arabic description acquires the wrapper the seeded rows already have.
    expect(host.control.value).toBe('<div dir="rtl"><p>جديد</p></div>');
  }));

  it('sanitises on the way out, not only on the way in', fakeAsync(() => {
    type('<p>Copy</p><span style="color:red">Loud</span><script>alert(1)</script>');
    tick(300);

    expect(host.control.value).not.toContain('script');
    expect(host.control.value).not.toContain('style=');
    expect(host.control.value).toContain('Loud');
  }));

  it('reports an emptied editor as empty, so required can fail on it', fakeAsync(() => {
    host.control.setValidators(Validators.required);
    host.control.setValue('<p>Fast</p>');
    fixture.detectChanges();

    type('<p><br></p>');
    tick(300);

    // Load-bearing: without the collapse, `required` passes on an editor with nothing in it.
    expect(host.control.value).toBe('');
    expect(host.control.hasError('required')).toBe(true);
  }));

  it('returns an untouched document byte for byte', fakeAsync(() => {
    /*
     * Browsers normalise whitespace and attribute order on parse, so a document nobody edited can
     * come back subtly different and show up as a diff on the seller's product.
     */
    const stored = '<div dir="rtl"><h2>العنوان</h2>\n<p>الوصف</p></div>';
    host.control.setValue(stored);
    fixture.detectChanges();

    editable().dispatchEvent(new Event('input'));
    tick(300);

    expect(host.control.value).toBe(stored);
  }));

  /* ------------------------------------------------------------------ caret guard ---- */

  it('does not rewrite the document under a caret', fakeAsync(() => {
    editable().dispatchEvent(new Event('focus'));
    type('<p>Half a sen</p>');
    tick(300);

    // The echo of our own emission coming back around the form; and then a genuine outside write.
    host.control.setValue('<p>Something else</p>', {emitEvent: false});
    fixture.detectChanges();

    // The operator is mid-word. The form is not the authority on what they have typed.
    expect(editable().innerHTML).toBe('<p>Half a sen</p>');
  }));

  it('takes an outside write once focus has left', fakeAsync(() => {
    editable().dispatchEvent(new Event('focus'));
    type('<p>Half a sen</p>');
    tick(300);
    editable().dispatchEvent(new Event('blur'));

    host.control.setValue('<p>Something else</p>');
    fixture.detectChanges();

    expect(editable().innerHTML).toBe('<p>Something else</p>');
  }));

  /* --------------------------------------------------------------------- toolbar ---- */

  it('is one tab stop, not one per button', () => {
    const stops = tools().filter((button) => button.tabIndex === 0);

    expect(tools().length).toBeGreaterThan(1);
    expect(stops.length).toBe(1);
    expect(stops[0]).toBe(tools()[0]);
  });

  it('moves the roving stop with the arrows, without activating anything', fakeAsync(() => {
    const toolbar = element.querySelector<HTMLElement>('.rt-toolbar')!;
    toolbar.dispatchEvent(new KeyboardEvent('keydown', {key: 'ArrowRight', bubbles: true}));
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    // A toolbar is not a tablist: arrows move focus, they do not apply the format.
    expect(tools()[1].tabIndex).toBe(0);
    expect(tools()[0].tabIndex).toBe(-1);
    expect(host.control.value).toBe('');
  }));

  it('keeps the caret when a button is pressed with the mouse', () => {
    /*
     * Without this the mousedown blurs the editable, the selection collapses, and the command lands
     * on nothing — the single most common way a hand-rolled toolbar appears to do nothing at all.
     */
    const event = new MouseEvent('mousedown', {bubbles: true, cancelable: true});
    tools()[0].dispatchEvent(event);

    expect(event.defaultPrevented).toBe(true);
  });

  it('labels every icon-only button', () => {
    for (const button of [...tools(), sourceToggle()]) {
      expect(button.getAttribute('aria-label')).toBeTruthy();
    }
    expect(tools()[2].getAttribute('aria-label')).toBe('Bold');
  });

  it('advertises the shortcut on the button that carries it', () => {
    expect(tools()[2].title).toBe('Bold · Ctrl+B');
  });

  /* ---------------------------------------------------------------- source view ---- */

  it('swaps to the HTML source with the value intact, and back again', fakeAsync(() => {
    host.control.setValue('<p>Fast</p>');
    fixture.detectChanges();

    sourceToggle().click();
    fixture.detectChanges();

    const area = element.querySelector<HTMLTextAreaElement>('.rt-source')!;
    expect(area.value).toBe('<p>Fast</p>');
    expect(element.querySelector('.rt-editable')).toBeNull();

    sourceToggle().click();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(editable().innerHTML).toBe('<p>Fast</p>');
  }));

  it('is a switch, because it changes the field’s mode rather than a format', () => {
    expect(sourceToggle().getAttribute('role')).toBe('switch');
    expect(sourceToggle().getAttribute('aria-checked')).toBe('false');

    sourceToggle().click();
    fixture.detectChanges();

    expect(sourceToggle().getAttribute('aria-checked')).toBe('true');
  });

  it('says that switching modes ends the undo history', () => {
    sourceToggle().click();
    fixture.detectChanges();

    const status = element.querySelector<HTMLElement>('[role="status"]')!;
    expect(status.textContent).toContain('Undo history starts again');
  });

  it('sanitises what is typed into the source view', () => {
    sourceToggle().click();
    fixture.detectChanges();

    const area = element.querySelector<HTMLTextAreaElement>('.rt-source')!;
    area.value = '<p onclick="steal()">Fast</p><script>alert(1)</script>';
    area.dispatchEvent(new Event('input'));

    // The source view is a convenience, not a hole in the allow-list.
    expect(host.control.value).toBe('<p>Fast</p>');
  });

  /* ------------------------------------------------------------------- disabled ---- */

  it('stops being editable when the form disables it', () => {
    host.control.setValue('<p>Fast</p>');
    host.control.disable();
    fixture.detectChanges();

    /*
     * `contenteditable` is dropped rather than set to `false`: a `contenteditable="false"` region
     * still reads as a textbox to a screen reader and still takes focus. `aria-readonly` says the
     * true thing.
     */
    expect(editable().hasAttribute('contenteditable')).toBe(false);
    expect(editable().getAttribute('aria-readonly')).toBe('true');
    expect(tools().every((button) => button.disabled)).toBe(true);
  });

  it('draws the error frame from the consumer\u2019s validity, never from its own guess', () => {
    const field = element.querySelector('app-rich-text')!;
    expect(field.classList).not.toContain('rich-text-invalid');

    /*
     * `invalid` is an input the consumer binds from `control.invalid && touched`. The component
     * never inspects the control itself, which is what keeps it usable outside a form and stops it
     * reddening a field the operator has not reached yet.
     */
    host.invalid.set(true);
    fixture.detectChanges();

    expect(field.classList).toContain('rich-text-invalid');
    expect(editable().getAttribute('aria-invalid')).toBe('true');
  });
});
