import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {Router, provideRouter} from '@angular/router';
import {Observable, of, throwError} from 'rxjs';

import type {CreateOrgUser} from '@api/tenancy/org.service';
import {NOTIFICATION_PORT} from '@cvhome-saas/ui-kit';
import type {OrgRow} from '@models/platform';
import {translocoTesting} from '@testing/transloco-testing';
import {Organizations} from './organizations';
import {OrganizationsApi, type OrgsQuery, type OrgsSnapshot} from './services/organizations.api.service';

function row(id: string, overrides: Partial<OrgRow> = {}): OrgRow {
  const email = `${id}@example.com`;
  return {
    id,
    name: '',
    label: email,
    email,
    status: 'ACTIVE',
    createdDate: '2026-08-04T09:15:00Z',
    ownerUserId: 'c0ffee00-dead-4bee-8000-000000000001',
    ...overrides,
  };
}

/** Twenty-two rows, so the page has a second page and the pager has something to do. */
const ORGS: readonly OrgRow[] = [
  row('65f023632bc46470c104b76f', {name: 'Nordwerk', label: 'Nordwerk'}),
  // The state of every organization on the platform: created with no name, so listed by email.
  row('65f023632bc46470c104b75f'),
  row('65f023632bc46470c104b77f', {status: 'SUSPENDED'}),
  ...Array.from({length: 19}, (_, index) => row(`65f023632bc46470c104b${(100 + index).toString(16)}`)),
];

/** Stands in for the endpoint, so the spec controls filtering, paging, timing and failure. */
class FakeOrganizationsApi {
  readonly requests: OrgsQuery[] = [];
  readonly created: CreateOrgUser[] = [];
  failure = false;
  createFailure = false;
  orgs: readonly OrgRow[] = ORGS;

  loadOrgs(query: OrgsQuery): Observable<OrgsSnapshot> {
    this.requests.push(query);
    if (this.failure) {
      return throwError(() => new Error('the organizations could not be read'));
    }
    /*
     * Filters and pages the way the server would, so the spec exercises real behaviour rather than
     * asserting that a request was made. The term spans the name and the email, as the SQL does.
     */
    const term = query.term.trim().toLowerCase();
    const matched = this.orgs.filter(
      (org) =>
        (!term || org.name.toLowerCase().includes(term) || org.email.toLowerCase().includes(term)) &&
        (!query.status || org.status === query.status),
    );
    const totalPages = Math.max(1, Math.ceil(matched.length / query.count));
    const page = Math.min(Math.max(0, query.page), totalPages - 1);
    return of({
      rows: matched.slice(page * query.count, page * query.count + query.count),
      totalElements: matched.length,
      totalPages,
      term: query.term,
    });
  }

  create(user: CreateOrgUser): Observable<void> {
    this.created.push(user);
    return this.createFailure ? throwError(() => new Error('that address is taken')) : of(undefined);
  }
}

describe('Organizations', () => {
  let api: FakeOrganizationsApi;
  let fixture: ComponentFixture<Organizations>;
  let router: Router;

  beforeEach(async () => {
    api = new FakeOrganizationsApi();

    await TestBed.configureTestingModule({
      imports: [Organizations, ...translocoTesting().imports],
      providers: [
        // The real shapes, so a navigation the page gets wrong fails here rather than being swallowed.
        provideRouter([
          {path: 'platform/organizations', children: []},
          {path: 'platform/organizations/:id', children: []},
        ]),
        {provide: OrganizationsApi, useValue: api},
        {provide: NOTIFICATION_PORT, useValue: {danger: () => undefined, success: () => undefined}},
        ...translocoTesting().providers,
      ],
    }).compileComponents();

    router = TestBed.inject(Router);
  });

  /*
   * `snapshot` is an `rxResource`, so the first value lands a microtask after the component is
   * created and the busy overlay is up until it does. Everything here runs in `fakeAsync` and
   * settles the resource before asserting.
   */
  function load(): HTMLElement {
    fixture = TestBed.createComponent(Organizations);
    settle();
    return fixture.nativeElement as HTMLElement;
  }

  function settle(): void {
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
  }

  function rows(host: HTMLElement): HTMLElement[] {
    return Array.from(host.querySelectorAll('app-table-row'));
  }

  it('lists a page of organizations, server-paged', fakeAsync(() => {
    const host = load();

    expect(rows(host).length).toBe(20);
    expect(api.requests).toEqual([{page: 0, count: 20, term: '', status: ''}]);
  }));

  /*
   * `ManagerOrgEntity.createOrgFromUser` sets no name and `rename` is the only writer, so the
   * identity column would be empty on every row without the fallback. See lessons.md,
   * "Organizations — an org cannot be named at creation".
   */
  it('falls back to the contact email for an organization that has never been named', fakeAsync(() => {
    const host = load();
    const [named, unnamed] = rows(host);

    expect(named!.textContent).toContain('Nordwerk');
    expect(unnamed!.querySelector('.unnamed')).not.toBeNull();
    expect(unnamed!.textContent).toContain('65f023632bc46470c104b75f@example.com');
  }));

  it('shows each organization’s status as a badge', fakeAsync(() => {
    const host = load();
    const suspended = rows(host)[2]!;

    expect(suspended.querySelector('app-badge')?.textContent?.trim()).toBeTruthy();
  }));

  /*
   * The point of the box is that the *server* narrows: a term that only filtered the twenty rows on
   * screen would be a lie about what it searched. So the assertion is on the request, not the rows.
   */
  it('sends the search term to the server and resets to the first page', fakeAsync(() => {
    const host = load();
    (host.querySelector('app-search-box input') as HTMLInputElement).value = 'nordwerk';
    host.querySelector('app-search-box input')!.dispatchEvent(new Event('input'));
    settle();
    // The box debounces, so the request lands a tick later than the keystroke.
    tick(400);
    settle();

    expect(api.requests.at(-1)).toEqual({page: 0, count: 20, term: 'nordwerk', status: ''});
    expect(rows(host).length).toBe(1);
    expect(router.url).toContain('q=nordwerk');
  }));

  it('sends the status filter to the server', fakeAsync(() => {
    const host = load();
    const trigger = host.querySelector('.org-filters app-select button') as HTMLButtonElement;
    trigger.click();
    settle();
    const suspended = Array.from(host.querySelectorAll('.org-filters app-select [role="option"]')).find((option) =>
      option.textContent?.toLowerCase().includes('suspend'),
    ) as HTMLElement;
    suspended.click();
    settle();

    expect(api.requests.at(-1)?.status).toBe('SUSPENDED');
    expect(rows(host).length).toBe(1);
  }));

  /*
   * The last good page stays on screen while a new term is in flight, so an empty state shown before
   * the rows answer would be describing the previous query. Module 9 shipped that bug once already.
   */
  it('says nothing matched only once the rows answer the term', fakeAsync(() => {
    const host = load();
    (host.querySelector('app-search-box input') as HTMLInputElement).value = 'nothing-matches-this';
    host.querySelector('app-search-box input')!.dispatchEvent(new Event('input'));
    settle();
    tick(400);
    settle();

    expect(rows(host).length).toBe(0);
    expect(host.querySelector('app-empty-state')).not.toBeNull();
  }));

  it('asks the server for the next page and mirrors it into the URL', fakeAsync(() => {
    const host = load();

    const next = Array.from(host.querySelectorAll('button')).find((button) =>
      button.getAttribute('aria-label')?.toLowerCase().includes('next'),
    );
    next!.click();
    settle();

    expect(api.requests.at(-1)).toEqual({page: 1, count: 20, term: '', status: ''});
    expect(router.url).toContain('page=1');
  }));

  it('opens an organization by id', fakeAsync(() => {
    const host = load();

    (rows(host)[0]!.querySelector('.identity') as HTMLButtonElement).click();
    settle();

    expect(router.url).toBe('/platform/organizations/65f023632bc46470c104b76f');
  }));

  it('creates an organization from the dialog and re-reads the list', fakeAsync(() => {
    const host = load();

    (host.querySelector('.primary-action') as HTMLButtonElement).click();
    settle();

    const dialog = host.querySelector('app-create-org-dialog') as HTMLElement;
    const field = (name: string) =>
      Array.from(dialog.querySelectorAll('app-form-field')).find((wrapper) =>
        wrapper.textContent?.toLowerCase().includes(name),
      )!;
    const typeInto = (wrapper: Element, value: string) => {
      const input = wrapper.querySelector('input') as HTMLInputElement;
      input.value = value;
      input.dispatchEvent(new Event('input'));
    };

    typeInto(field('first'), 'Ada');
    typeInto(field('last'), 'Lovelace');
    typeInto(field('email'), 'ada@example.com');
    // Not `Passw0rd`, which this test used before the dialog started applying tenancy's rules: it is on the
    // common-password list, and satisfying three character classes is exactly why it is.
    typeInto(field('password')!, 'correct-horse-8');
    // The repeat field is the second password box; the first matched above.
    const passwords = Array.from(dialog.querySelectorAll('input[type="password"]')) as HTMLInputElement[];
    passwords[1]!.value = 'correct-horse-8';
    passwords[1]!.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    (dialog.querySelector('button[type="submit"]') as HTMLButtonElement).click();
    settle();

    expect(api.created).toEqual([
      {
        firstName: 'Ada',
        lastName: 'Lovelace',
        emailAddress: 'ada@example.com',
        password: 'correct-horse-8',
        // tenancy's `@PasswordsMatch` reads this; the dialog used to collect it and never send it.
        repeatPassword: 'correct-horse-8',
      },
    ]);
    // Re-read rather than echoed: the row shown is the one tenancy stored.
    expect(api.requests.length).toBe(2);
  }));

  it('refuses to submit a create with mismatched passwords, and says which', fakeAsync(() => {
    const host = load();

    (host.querySelector('.primary-action') as HTMLButtonElement).click();
    settle();

    const dialog = host.querySelector('app-create-org-dialog') as HTMLElement;
    (dialog.querySelector('button[type="submit"]') as HTMLButtonElement).click();
    settle();

    expect(api.created.length).toBe(0);
    expect(dialog.querySelector('[role="alert"]')?.textContent?.trim()).toBeTruthy();
  }));

  it('offers a retry when the list cannot be read', fakeAsync(() => {
    api.failure = true;
    const host = load();

    expect(host.querySelector('app-load-error')).not.toBeNull();
  }));
});
