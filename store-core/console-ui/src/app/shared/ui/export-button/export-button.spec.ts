import {Component, ElementRef, signal, viewChild} from '@angular/core';
import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';

import {PdfExportService, type PdfExportRequest} from '@core/export/pdf-export.service';
import {translocoTesting} from '@testing/transloco-testing';
import {Panel} from '@cvhome-saas/ui-kit/ui';
import {ExportButton} from './export-button';

class FakePdfExportService {
  readonly requests: PdfExportRequest[] = [];
  failure: Error | null = null;
  private resolvePending: (() => void) | null = null;
  /** When true, `export` hangs until `finish()` — used to observe the busy state. */
  deferred = false;

  export(request: PdfExportRequest): Promise<void> {
    this.requests.push(request);
    if (this.failure) {
      return Promise.reject(this.failure);
    }
    if (this.deferred) {
      return new Promise<void>((resolve) => (this.resolvePending = resolve));
    }
    return Promise.resolve();
  }

  finish(): void {
    this.resolvePending?.();
    this.resolvePending = null;
  }
}

@Component({
  imports: [ExportButton],
  template: `
    <app-export-button
      [target]="region()"
      [fileName]="'report'"
      [title]="'Orders'"
      [subtitle]="'Jul 1 – Jul 31'"
      [disabled]="locked()"
    />
    @if (showRegion()) {
      <div #region class="region">{{ 'widgets' }}</div>
    }
  `,
})
class Host {
  readonly showRegion = signal(true);
  readonly locked = signal(false);
  readonly region = viewChild<ElementRef<HTMLElement>>('region');
}

/*
 * The products page anchored its `#report` on `<app-panel>` rather than a `<div>`, and a template
 * reference on a *component* element resolves to the component instance — so `target` received a
 * `Panel` object, and Export did nothing at all. Orders, order details and the dashboard happened
 * to anchor theirs on plain elements and so happened to work. This host reproduces the anchor.
 */
@Component({
  imports: [ExportButton, Panel],
  template: `
    <app-export-button [target]="panelElement()" [fileName]="'report'" [title]="'Products'" />
    <app-panel #panelRef [title]="'All products'">rows</app-panel>
  `,
})
class ComponentAnchoredHost {
  /*
   * The reference and the property are named differently on purpose: a template reference shadows
   * a component property of the same name *within its scope*, so `report()` beside a `#report`
   * resolves to the reference and blows up. Products got away with it only because its `#report`
   * lives inside an `@if` block that the binding sits outside of.
   */
  readonly panelElement = viewChild('panelRef', {read: ElementRef});
}

describe('ExportButton', () => {
  let pdf: FakePdfExportService;

  beforeEach(async () => {
    pdf = new FakePdfExportService();
    await TestBed.configureTestingModule({
      imports: [Host, ComponentAnchoredHost, ...translocoTesting().imports],
      providers: [{provide: PdfExportService, useValue: pdf}, ...translocoTesting().providers],
    }).compileComponents();
  });

  function host(): {fixture: ComponentFixture<Host>; element: HTMLElement} {
    const fixture = TestBed.createComponent(Host);
    fixture.detectChanges();
    return {fixture, element: fixture.nativeElement as HTMLElement};
  }

  function button(element: HTMLElement): HTMLButtonElement {
    return element.querySelector('.export-button') as HTMLButtonElement;
  }

  it('exports the targeted region with the given file name and header', fakeAsync(() => {
    const {fixture, element} = host();

    button(element).click();
    tick();
    fixture.detectChanges();

    expect(pdf.requests.length).toBe(1);
    expect(pdf.requests[0].fileName).toBe('report');
    expect(pdf.requests[0].title).toBe('Orders');
    expect(pdf.requests[0].subtitle).toBe('Jul 1 – Jul 31');
    expect(pdf.requests[0].element.classList).toContain('region');
  }));

  it('exports an element even when the reference is anchored on a component', fakeAsync(() => {
    const fixture = TestBed.createComponent(ComponentAnchoredHost);
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    (element.querySelector('app-export-button button') as HTMLButtonElement).click();
    tick();
    fixture.detectChanges();

    expect(pdf.requests.length).toBe(1);
    // An element, not a `Panel`. `viewChild(..., {read: ElementRef})` is what makes that true.
    expect(pdf.requests[0].element instanceof HTMLElement).toBeTrue();
    expect(pdf.requests[0].element.tagName.toLowerCase()).toBe('app-panel');
  }));

  it('stays disabled until a target exists', () => {
    const {fixture, element} = host();
    expect(button(element).disabled).toBeFalse();

    fixture.componentInstance.showRegion.set(false);
    fixture.detectChanges();

    expect(button(element).disabled).toBeTrue();
  });

  it('honours an external disabled state', () => {
    const {fixture, element} = host();

    fixture.componentInstance.locked.set(true);
    fixture.detectChanges();

    expect(button(element).disabled).toBeTrue();
  });

  it('shows a busy state and blocks repeat clicks while preparing', fakeAsync(() => {
    pdf.deferred = true;
    const {fixture, element} = host();

    button(element).click();
    fixture.detectChanges();

    expect(button(element).getAttribute('aria-busy')).toBe('true');
    expect(button(element).disabled).toBeTrue();
    expect(button(element).textContent).toContain('Preparing…');

    // A second click must not queue another export.
    button(element).click();
    fixture.detectChanges();
    expect(pdf.requests.length).toBe(1);

    pdf.finish();
    tick();
    fixture.detectChanges();

    expect(button(element).getAttribute('aria-busy')).toBe('false');
    expect(button(element).disabled).toBeFalse();
    expect(button(element).textContent).toContain('Export');
  }));

  it('surfaces a failure and recovers on the next attempt', fakeAsync(() => {
    pdf.failure = new Error('Unable to prepare the export canvas.');
    const {fixture, element} = host();

    button(element).click();
    tick();
    fixture.detectChanges();

    const alert = element.querySelector('[role="alert"]');
    expect(alert?.textContent).toContain('Unable to prepare the export canvas.');
    expect(button(element).disabled).toBeFalse();

    pdf.failure = null;
    button(element).click();
    tick();
    fixture.detectChanges();

    expect(element.querySelector('[role="alert"]')).toBeNull();
  }));

  it('announces the outcome for assistive tech', fakeAsync(() => {
    const {fixture, element} = host();

    button(element).click();
    tick();
    fixture.detectChanges();

    expect(element.querySelector('[role="status"]')?.textContent).toContain('Export ready');
  }));
});
