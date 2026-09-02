import {TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Observable, of, throwError} from 'rxjs';

import {NOTIFICATION_PORT} from '@cvhome-saas/ui-kit';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import type {PersistableProductOption} from '@models/catalog';
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
  options: [
    {
      id: 9,
      code: 'color',
      name: 'Color',
      sortOrder: 0,
      copy: [
        {language: 'en', name: 'Color'},
        {language: 'ar', name: 'اللون'},
      ],
      values: [
        {
          id: 91,
          code: 'red',
          name: 'Red',
          sortOrder: 0,
          copy: [
            {language: 'en', name: 'Red'},
            {language: 'ar', name: 'أحمر'},
          ],
        },
      ],
    },
  ],
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

  optionCodeTaken(): Observable<boolean> {
    return of(false);
  }

  readonly optionBodies: PersistableProductOption[] = [];

  createOption(body: PersistableProductOption): Observable<CatalogueSnapshot> {
    this.optionBodies.push(body);
    return of(this.current);
  }

  updateOption(_id: number, body: PersistableProductOption): Observable<CatalogueSnapshot> {
    this.optionBodies.push(body);
    return of(this.current);
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
        // Page-provided in production; the spec provides it directly to test the facade alone.
        CatalogueFacade,
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

  /* --------------------------------------------------------------- option editor ---- */

  it('loads the selected option with its value rows, existing codes locked', fakeAsync(() => {
    settle();
    facade.activeTab.set('options');
    settle();

    expect(facade.optionForm.controls.code.value).toBe('color');
    expect(facade.optionForm.controls.code.disabled).toBe(true);
    expect(facade.optionForm.controls.name.value).toBe('Color');
    const rows = facade.optionForm.controls.values.controls;
    expect(rows.length).toBe(1);
    expect(rows[0].controls.name.value).toBe('Red');
    // An existing value's code is its identity — renaming happens through the name, not the code.
    expect(rows[0].controls.code.disabled).toBe(true);
  }));

  it('keeps an unsaved value name across a language switch, and an added row too', fakeAsync(() => {
    settle();
    facade.activeTab.set('options');
    settle();

    facade.optionForm.controls.values.at(0).controls.name.setValue('Crimson');
    facade.addOptionValue();
    facade.optionForm.controls.values.at(1).controls.name.setValue('Blue');

    facade.setLanguage('ar');
    settle();
    expect(facade.optionForm.controls.values.at(0).controls.name.value).toBe('أحمر');
    // The added row is structure, not copy: it must survive the switch, blank in Arabic.
    expect(facade.optionForm.controls.values.length).toBe(2);
    expect(facade.optionForm.controls.values.at(1).controls.name.value).toBe('');

    facade.setLanguage('en');
    settle();
    expect(facade.optionForm.controls.values.at(0).controls.name.value).toBe('Crimson');
    expect(facade.optionForm.controls.values.at(1).controls.name.value).toBe('Blue');
  }));

  it('sends every language for the option and for each value, ids kept', fakeAsync(() => {
    settle();
    facade.activeTab.set('options');
    settle();

    facade.optionForm.controls.name.setValue('Colour');
    facade.saveOption();
    settle();

    const [body] = api.optionBodies;
    expect(body.code).toBe('color');
    expect(body.descriptions.find((entry) => entry.language === 'en')?.name).toBe('Colour');
    // Untouched Arabic goes back exactly as it arrived — the write replaces the whole list.
    expect(body.descriptions.find((entry) => entry.language === 'ar')?.name).toBe('اللون');
    const [value] = body.values;
    // The id is what keeps the store-wide value id stable for every variant referencing it.
    expect(value.id).toBe(91);
    expect(value.descriptions.map((entry) => entry.language).sort()).toEqual(['ar', 'en']);
  }));

  it('refuses to save an option with no values', fakeAsync(() => {
    settle();
    facade.activeTab.set('options');
    settle();
    facade.startCreate();
    facade.optionForm.controls.code.setValue('size');
    facade.optionForm.controls.name.setValue('Size');
    tick(500);

    facade.saveOption();
    settle();

    // Nothing was sent: an option with no values has nothing to generate variants from.
    expect(api.optionBodies.length).toBe(0);
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
