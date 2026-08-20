import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';

import {provideRouter} from '@angular/router';
import {Observable, Subject, of, throwError} from 'rxjs';

import type {FirstRunSnapshot} from '@models/first-run';
import {translocoTesting} from '@testing/transloco-testing';
import {FirstRun} from './first-run';
import {FirstRunFacade} from './facades/first-run.facade';
import {SubscriptionService} from '@api/billing/subscription.service';
import {FirstRunApi} from './services/first-run.api.service';

function snapshot(): FirstRunSnapshot {
  return {
    steps: [
      {id: 'create-store', labelKey: 'firstRun.step.createStore.label', metaKey: 'firstRun.step.createStore.meta'},
      {id: 'add-products', labelKey: 'firstRun.step.addProducts.label', metaKey: 'firstRun.step.addProducts.meta'},
      {id: 'design-home', labelKey: 'firstRun.step.designHome.label', metaKey: 'firstRun.step.designHome.meta'},
    ],
    guides: [
      {
        id: 'csv-import',
        titleKey: 'firstRun.guide.csvImport.title',
        durationKey: 'firstRun.guide.csvImport.duration',
        sectionKey: 'firstRun.guide.section.products',
        docPath: 'products/import-csv/',
      },
    ],
    nextUp: [
      {id: 'catalogue', titleKey: 'firstRun.nextUp.catalogue.title', copyKey: 'firstRun.nextUp.catalogue.copy', icon: 'box'},
    ],
    limits: [
      {id: 'stores', labelKey: 'firstRun.limit.stores', used: '0', cap: '1', pct: 0, noteKey: 'firstRun.limit.growthAllows', noteParams: {amount: '5'}},
    ],
    feature: {
      titleKey: 'firstRun.feature.title',
      copyKey: 'firstRun.feature.copy',
      durationKey: 'firstRun.feature.duration',
      src: 'https://example.test/walkthrough.mp4',
      poster: null,
    },
    trialDays: 14,
  };
}

/** Stands in for the real endpoint so the spec controls timing and failure. */
class FakeFirstRunApi {
  calls = 0;
  deferred = false;
  pending: Subject<FirstRunSnapshot> | null = null;
  failure: Error | null = null;

  loadSnapshot(): Observable<FirstRunSnapshot> {
    this.calls += 1;
    if (this.failure) {
      return throwError(() => this.failure);
    }
    if (this.deferred) {
      this.pending = new Subject<FirstRunSnapshot>();
      return this.pending;
    }
    return of(snapshot());
  }

  resolve(): void {
    this.pending?.next(snapshot());
    this.pending?.complete();
    this.pending = null;
  }
}

/**
 * The page owns only its own content — the disabled rail and the redirects that lead here
 * are covered by `console-shell.spec.ts` and `first-run.guard.spec.ts`.
 */
describe('FirstRun', () => {
  let api: FakeFirstRunApi;

  beforeEach(async () => {
    // The trial flag is persisted, so a case that starts it must not leak into the next.
    localStorage.removeItem('cvhome.console.trialStarted');
    api = new FakeFirstRunApi();
    await TestBed.configureTestingModule({
      imports: [FirstRun, ...translocoTesting().imports],
      providers: [
        provideRouter([]),
        /*
         * The page now mounts `PlanDialog`, which injects the billing catalogue. A stub rather
         * than the HTTP testing stack: `CrudService` pulls in the request context and the whole
         * interceptor chain, none of which this page's behaviour depends on. The dialog's own
         * spec covers the catalogue.
         */
        {provide: SubscriptionService, useValue: {plans: () => of([])}},
        {provide: FirstRunApi, useValue: api},
        ...translocoTesting().providers,
      ],
    }).compileComponents();
  });

  function load(): {fixture: ComponentFixture<FirstRun>; element: HTMLElement} {
    const fixture = TestBed.createComponent(FirstRun);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return {fixture, element: fixture.nativeElement as HTMLElement};
  }

  it('renders every section of the getting-started page', fakeAsync(() => {
    const {element} = load();

    expect(element.querySelector('.hero')).not.toBeNull();
    expect(element.querySelector('.progress-card')).not.toBeNull();
    expect(element.querySelector('.feature-video')).not.toBeNull();
    expect(element.querySelectorAll('.guide-row').length).toBe(1);
    expect(element.querySelectorAll('.next-up-card').length).toBe(1);
    expect(element.querySelectorAll('.limit').length).toBe(1);
    expect(element.querySelector('.help')).not.toBeNull();

    // Chrome belongs to the shell; a page must not grow its own.
    expect(element.querySelector('.sidebar')).toBeNull();
    expect(element.querySelector('app-plan-banner')).toBeNull();
  }));

  it('renders the checklist with only the first step live and the rest locked', fakeAsync(() => {
    const {element} = load();

    const rows = element.querySelectorAll('.task-list li');
    expect(rows.length).toBe(3);
    expect(rows[0].classList).toContain('active');
    expect(rows[0].querySelector('app-icon[name="lock"]')).toBeNull();

    // The two that depend on a store are locked, and say so rather than just dimming.
    expect(rows[1].classList).not.toContain('active');
    expect(rows[1].querySelector('.task-lock')).not.toBeNull();
    expect(rows[2].querySelector('.task-lock')).not.toBeNull();
  }));

  describe('the trial gate', () => {
    it('withholds store creation until the trial is started', fakeAsync(() => {
      const {element} = load();

      expect(element.querySelector('a[href="/store-management/create"]')).toBeNull();
      const inert = element.querySelector('.primary-action.disabled');
      expect(inert).not.toBeNull();
      expect(inert?.getAttribute('aria-disabled')).toBe('true');
      // Disabled is not enough on its own — the control has to say why.
      expect(inert?.getAttribute('title')).toContain('trial');
    }));

    it('turns the gate into a real link once the trial starts', fakeAsync(() => {
      const {fixture, element} = load();

      (element.querySelector('.trial-start') as HTMLButtonElement).click();
      fixture.detectChanges();

      expect(element.querySelector('.primary-action.disabled')).toBeNull();
      expect(element.querySelector('a[href="/store-management/create"]')).not.toBeNull();
    }));

    it('flips the notice from waiting to running when the trial starts', fakeAsync(() => {
      const {fixture, element} = load();

      const notice = element.querySelector('app-notice-bar')!;
      expect(notice.classList).toContain('amber');
      expect(notice.querySelector('.notice-copy')?.textContent).toContain('Start your 14-day trial');

      (element.querySelector('.trial-start') as HTMLButtonElement).click();
      fixture.detectChanges();

      expect(notice.classList).toContain('green');
      expect(notice.querySelector('.notice-copy')?.textContent).toContain('14 days left');
      expect(element.querySelector('.trial-start')).toBeNull();
    }));

    it('remembers a started trial across a reload', fakeAsync(() => {
      const {fixture, element} = load();
      (element.querySelector('.trial-start') as HTMLButtonElement).click();
      fixture.detectChanges();

      // A new facade is what a fresh page load produces; the flag has to survive it.
      TestBed.resetTestingModule();
      TestBed.configureTestingModule({
        imports: [...translocoTesting().imports],
        providers: [
        provideRouter([]),
        /*
         * The page now mounts `PlanDialog`, which injects the billing catalogue. A stub rather
         * than the HTTP testing stack: `CrudService` pulls in the request context and the whole
         * interceptor chain, none of which this page's behaviour depends on. The dialog's own
         * spec covers the catalogue.
         */
        {provide: SubscriptionService, useValue: {plans: () => of([])}},
        {provide: FirstRunApi, useValue: api},
        ...translocoTesting().providers,
      ],
      });

      expect(TestBed.inject(FirstRunFacade).trialPending()).toBeFalse();
    }));
  });

  it('veils the page until the first response arrives', fakeAsync(() => {
    api.deferred = true;
    const fixture = TestBed.createComponent(FirstRun);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    expect(element.querySelector('app-busy-overlay')?.getAttribute('aria-busy')).toBe('true');
    expect(element.querySelector('.hero')).toBeNull();

    api.resolve();
    tick();
    fixture.detectChanges();

    expect(element.querySelector('app-busy-overlay')?.getAttribute('aria-busy')).toBe('false');
    expect(element.querySelector('.hero')).not.toBeNull();
  }));

  it('surfaces a failed request with a retry that refetches', fakeAsync(() => {
    api.failure = new Error('Unable to load your setup.');
    const fixture = TestBed.createComponent(FirstRun);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    const element = fixture.nativeElement as HTMLElement;

    const alert = element.querySelector('[role="alert"]');
    expect(alert?.textContent).toContain('Unable to load your setup.');

    api.failure = null;
    (element.querySelector('[role="alert"] button') as HTMLButtonElement).click();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(element.querySelector('[role="alert"]')).toBeNull();
    expect(element.querySelector('.hero')).not.toBeNull();
  }));

  it('sends each short guide to the documentation site rather than a dead button', fakeAsync(() => {
    const {element} = load();

    const guide = element.querySelector<HTMLAnchorElement>('.guide-row');
    // A link, not a button: these rows used to raise a "not available yet" toast while the written
    // guides already existed on the docs site.
    expect(guide?.tagName).toBe('A');
    expect(guide?.href).toBe('https://cvhome-saas.github.io/products/import-csv/');
    expect(guide?.target).toBe('_blank');
    // The console holds a session, so a docs tab must not be able to reach back into this window.
    expect(guide?.rel).toContain('noopener');
  }));

  it('opens a real player on the walkthrough instead of apologising', fakeAsync(() => {
    const {fixture, element} = load();

    expect(element.querySelector('app-video-dialog')).not.toBeNull();

    element.querySelector<HTMLButtonElement>('.feature-video')?.click();
    fixture.detectChanges();

    const dialog = element.querySelector<HTMLDialogElement>('dialog.vd');
    expect(dialog?.open).toBeTrue();
    expect(dialog?.querySelector('source')?.getAttribute('src')).toBe('https://example.test/walkthrough.mp4');
  }));

  it('opens the plan catalogue from the trial notice and from the plan panel', fakeAsync(() => {
    const {fixture, element} = load();

    // Both entry points raised a "not available yet" toast while billing had been serving the
    // catalogue to the marketing page all along.
    element.querySelector<HTMLButtonElement>('.trial-plans')?.click();
    fixture.detectChanges();
    expect(element.querySelector<HTMLDialogElement>('dialog.pd')?.open).toBeTrue();
  }));
});
