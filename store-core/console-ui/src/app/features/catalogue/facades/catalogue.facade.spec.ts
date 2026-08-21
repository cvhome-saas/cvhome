import {TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Observable, of, throwError} from 'rxjs';

import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import type {CatalogueSnapshot, CategoryNode, LocalisedCopy} from '@models/taxonomy';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {provideFakeProductSearch} from '@testing/product-search.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {CatalogueApi} from '../services/catalogue.api.service';
import {CatalogueFacade} from './catalogue.facade';

function copy(language: string, name: string, description = ''): LocalisedCopy {
  return {
    language,
    name,
    description,
    friendlyUrl: '',
    title: '',
    metaDescription: '',
    highlights: '',
    keyWords: '',
  };
}

function node(id: number, code: string, copies: LocalisedCopy[]): CategoryNode {
  return {
    id,
    code,
    name: copies[0]?.name ?? code,
    visible: true,
    sortOrder: 0,
    depth: 0,
    parentId: null,
    productCount: 0,
    totalCount: 0,
    copy: copies,
    children: [],
  };
}

/** Two categories: one written in both languages, one written only in English. */
const SNAPSHOT: CatalogueSnapshot = {
  categories: [
    node(1, 'MEN', [copy('en', 'Men', 'For men'), copy('ar', 'رجال', 'للرجال')]),
    node(2, 'KIDS', [copy('en', 'Kids')]),
  ],
  brands: [],
  types: [],
  groups: [],
  languages: ['en', 'ar'],
  unavailable: [],
};

class FakeCatalogueApi {
  current: CatalogueSnapshot = SNAPSHOT;
  /** Every copy list the facade sent to a write, so the merge can be inspected. */
  readonly saved: readonly LocalisedCopy[][] = [];
  takenCodes: string[] = ['MEN'];
  checkFails = false;

  load(): Observable<CatalogueSnapshot> {
    return of(this.current);
  }

  updateCategory(_id: number, copies: readonly LocalisedCopy[]): Observable<CatalogueSnapshot> {
    (this.saved as LocalisedCopy[][]).push([...copies]);
    return of(this.current);
  }

  createCategory(_copies: readonly LocalisedCopy[]): Observable<CatalogueSnapshot> {
    return of(this.current);
  }

  categoryCodeTaken(code: string): Observable<boolean> {
    if (this.checkFails) {
      return throwError(() => new Error('unique endpoint is down'));
    }
    return of(this.takenCodes.includes(code));
  }

  brandCodeTaken(): Observable<boolean> {
    return of(false);
  }

  typeCodeTaken(): Observable<boolean> {
    return of(false);
  }

  groupCodeTaken(): Observable<boolean> {
    return of(false);
  }
}

describe('CatalogueFacade', () => {
  let facade: CatalogueFacade;
  let api: FakeCatalogueApi;

  beforeEach(() => {
    localStorage.removeItem('cvhome.console.store');
    api = new FakeCatalogueApi();

    TestBed.configureTestingModule({
      imports: [...translocoTesting().imports],
      providers: [
        // `ConsoleShellFacade` reads the route for its breadcrumb, so the facade needs a router.
        provideRouter([]),
        {provide: CatalogueApi, useValue: api},
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})},
        {provide: NOTIFICATION_PORT, useValue: {danger: () => undefined}},
        provideFakeProductSearch(),
        ...translocoTesting().providers,
      ],
    });
    facade = TestBed.inject(CatalogueFacade);
  });

  /** Settles the store directory, the snapshot resource and the form-filling effect. */
  function settle(): void {
    TestBed.tick();
    tick();
    TestBed.tick();
  }

  it('loads the first category into the form without being asked', fakeAsync(() => {
    /*
     * The bug this guards: nothing filled the form on the first response, so the editor rendered
     * empty beside a fully populated tree until the operator clicked something.
     */
    settle();

    expect(facade.categoryForm.controls.code.value).toBe('MEN');
    expect(facade.categoryForm.controls.copy.controls.name.value).toBe('Men');
  }));

  it('disables the code of a record that already exists', fakeAsync(() => {
    settle();

    // A code identifies the record; changing it is a different record, not an edit.
    expect(facade.categoryForm.controls.code.disabled).toBe(true);
  }));

  it('keeps unsaved copy when the language changes and back again', fakeAsync(() => {
    settle();

    facade.categoryForm.controls.copy.controls.name.setValue('Menswear');
    facade.setLanguage('ar');
    settle();

    expect(facade.categoryForm.controls.copy.controls.name.value).toBe('رجال');

    facade.setLanguage('en');
    settle();

    // Typing an English name, switching to Arabic and back is what a translator does all day.
    expect(facade.categoryForm.controls.copy.controls.name.value).toBe('Menswear');
  }));

  it('discards the draft when a different record is opened', fakeAsync(() => {
    settle();
    facade.categoryForm.controls.copy.controls.name.setValue('Menswear');

    facade.select('categories', 2);
    settle();
    expect(facade.categoryForm.controls.copy.controls.name.value).toBe('Kids');

    facade.select('categories', 1);
    settle();
    // Back to what the server said, not to the abandoned edit.
    expect(facade.categoryForm.controls.copy.controls.name.value).toBe('Men');
  }));

  it('sends every language, not just the one on screen', fakeAsync(() => {
    settle();
    facade.categoryForm.controls.copy.controls.name.setValue('Menswear');
    facade.saveCategory();
    settle();

    const sent = api.saved[0];
    // These endpoints replace the description list, so a language left out is a language cleared.
    expect(sent.map((entry) => entry.language).sort()).toEqual(['ar', 'en']);
    expect(sent.find((entry) => entry.language === 'en')?.name).toBe('Menswear');
    // Untouched, and it must go back exactly as it arrived.
    expect(sent.find((entry) => entry.language === 'ar')?.name).toBe('رجال');
    expect(sent.find((entry) => entry.language === 'ar')?.description).toBe('للرجال');
  }));

  it('marks the language chips from what is written, not from what is on screen', fakeAsync(() => {
    settle();

    expect([...facade.translatedLanguages()].sort()).toEqual(['ar', 'en']);

    facade.select('categories', 2);
    settle();
    // Kids has only English.
    expect([...facade.translatedLanguages()]).toEqual(['en']);
  }));

  it('counts the whole tree once, so the header and the tab cannot disagree', fakeAsync(() => {
    settle();

    expect(facade.categoryCount()).toBe(2);
    const badge = facade.tabs().find((tab) => tab.key === 'categories')?.badge;
    expect(badge).toBe(String(facade.categoryCount()));
  }));

  /* ------------------------------------------------------------- uniqueness check ---- */

  it('marks a code that is already taken', fakeAsync(() => {
    settle();
    facade.startCreate();

    facade.categoryForm.controls.code.setValue('MEN');
    tick(500);

    expect(facade.categoryForm.controls.code.hasError('codeTaken')).toBe(true);
  }));

  it('accepts a code that is free', fakeAsync(() => {
    settle();
    facade.startCreate();

    facade.categoryForm.controls.code.setValue('WOMEN');
    tick(500);

    expect(facade.categoryForm.controls.code.hasError('codeTaken')).toBe(false);
    expect(facade.categoryForm.controls.code.valid).toBe(true);
  }));

  it('asks once for a run of keystrokes rather than once each', fakeAsync(() => {
    settle();
    facade.startCreate();
    let asked = 0;
    const original = api.categoryCodeTaken.bind(api);
    api.categoryCodeTaken = (code: string) => {
      asked += 1;
      return original(code);
    };

    facade.categoryForm.controls.code.setValue('M');
    tick(100);
    facade.categoryForm.controls.code.setValue('ME');
    tick(100);
    facade.categoryForm.controls.code.setValue('MEN');
    tick(500);

    expect(asked).toBe(1);
  }));

  it('leaves the field usable when the check itself cannot be made', fakeAsync(() => {
    /*
     * A lookup that timed out is not a code that is taken. Locking the field on an unreachable
     * endpoint is the worse failure — the server still has the last word on the save.
     */
    settle();
    facade.startCreate();
    api.checkFails = true;

    facade.categoryForm.controls.code.setValue('WOMEN');
    tick(500);

    expect(facade.categoryForm.controls.code.hasError('codeTaken')).toBe(false);
    expect(facade.categoryForm.controls.code.valid).toBe(true);
  }));

  it('refuses a code with characters that would break a URL', fakeAsync(() => {
    settle();
    facade.startCreate();

    facade.categoryForm.controls.code.setValue('men/shoes');
    tick(500);

    expect(facade.categoryForm.controls.code.hasError('pattern')).toBe(true);
    // And it never asked: the sync validators already say the answer cannot matter.
    expect(facade.categoryForm.controls.code.hasError('codeTaken')).toBe(false);
  }));

  it('never marks an existing record as a duplicate of itself', fakeAsync(() => {
    /*
     * The check starts while the form is being filled and the facade disables the code a tick later.
     * `disable()` nulls the errors it finds and cannot null one still in flight, so the answer
     * landed on a disabled control and every existing category showed a red "already taken" against
     * its own code — one it cannot even be edited to change.
     */
    api.takenCodes = ['MEN'];
    settle();
    tick(500);

    expect(facade.categoryForm.controls.code.disabled).toBe(true);
    expect(facade.categoryForm.controls.code.hasError('codeTaken')).toBe(false);
  }));

  it('suggests a code from the name while creating, and stops once one is typed', fakeAsync(() => {
    settle();
    facade.startCreate();

    facade.suggestCode('categories', 'Winter Coats');
    expect(facade.categoryForm.controls.code.value).toBe('winter-coats');

    facade.categoryForm.controls.code.markAsDirty();
    facade.suggestCode('categories', 'Something Else');
    // A suggestion, not a rule: once the operator has touched it, it is theirs.
    expect(facade.categoryForm.controls.code.value).toBe('winter-coats');
  }));
});
