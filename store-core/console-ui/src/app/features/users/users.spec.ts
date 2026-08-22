import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {Observable, Subject, of, throwError} from 'rxjs';

import {AuthService} from '@core/auth/auth.service';
import {NOTIFICATION_PORT} from '@core/errors/notification.port';
import {ConsoleApi} from '@layouts/console-shell/services/console.api.service';
import type {TeamRow} from '@models/team';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {Users} from './users';
import {UsersApi, type InvitationsSnapshot, type TeamQuery, type TeamSnapshot} from './services/users.api.service';
import {PAGE_SIZE} from './facades/users.facade';

function row(id: string, userName: string, overrides: Partial<TeamRow> = {}): TeamRow {
  return {
    id,
    name: `${userName} Person`,
    firstName: userName,
    lastName: 'Person',
    userName,
    email: `${userName}@mail.com`,
    roles: ['STORE_ADMIN'],
    active: true,
    store: 'ORG1-STORE1',
    isSelf: false,
    ...overrides,
  };
}

const TEAM: readonly TeamRow[] = [
  row('id-1', 'org1-store1-admin', {isSelf: true}),
  row('id-2', 'priya', {roles: ['STORE_MODERATOR'], active: false}),
  row('id-3', 'sven', {roles: []}),
];

/** Stands in for the endpoint so the spec controls paging, timing and failure. */
class FakeUsersApi {
  readonly requests: TeamQuery[] = [];
  readonly created: unknown[] = [];
  readonly updated: unknown[] = [];
  readonly resets: {userId: string; password: string}[] = [];
  readonly actives: {userId: string; active: boolean}[] = [];
  readonly deleted: string[] = [];
  /** When set, list requests hang until `resolve()` — used to observe the loading state. */
  pending: Subject<TeamSnapshot> | null = null;
  failure = false;
  team: readonly TeamRow[] = TEAM;
  roles: readonly string[] = ['STORE_MODERATOR', 'STORE_ADMIN'];

  loadTeam(query: TeamQuery): Observable<TeamSnapshot> {
    this.requests.push(query);
    if (this.failure) {
      return throwError(() => new Error('the team could not be read'));
    }
    if (this.pending) {
      return this.pending;
    }
    return of(this.snapshot(query));
  }

  loadInvitations(): Observable<InvitationsSnapshot> {
    return of({rows: [], pending: 0});
  }

  create(user: unknown): Observable<void> {
    this.created.push(user);
    return of(undefined);
  }

  update(user: unknown): Observable<void> {
    this.updated.push(user);
    return of(undefined);
  }

  resetPassword(userId: string, password: string): Observable<void> {
    this.resets.push({userId, password});
    return of(undefined);
  }

  setActive(userId: string, active: boolean): Observable<void> {
    this.actives.push({userId, active});
    return of(undefined);
  }

  delete(userId: string): Observable<void> {
    this.deleted.push(userId);
    return of(undefined);
  }

  resolve(query: TeamQuery = this.requests[this.requests.length - 1]): void {
    const subject = this.pending;
    this.pending = null;
    subject?.next(this.snapshot(query));
    subject?.complete();
  }

  /** Pages the team the way the server would, so the spec exercises real behaviour. */
  private snapshot(query: TeamQuery): TeamSnapshot {
    const size = query.page.count;
    const totalPages = Math.max(1, Math.ceil(this.team.length / size));
    const pageNumber = Math.min(Math.max(0, query.page.page), totalPages - 1);
    return {
      rows: this.team.slice(pageNumber * size, pageNumber * size + size),
      totalElements: this.team.length,
      totalPages,
      assignableRoles: this.roles,
    };
  }
}

describe('Users', () => {
  let api: FakeUsersApi;
  let fixture: ComponentFixture<Users>;
  let toasts: {messages: string[]; danger(text: string): void};
  let roles: {isSuperAdmin: boolean; isSupport: boolean; isOrgAdmin: boolean; isStoreAdmin: boolean; isStoreModerator: boolean};

  beforeEach(async () => {
    localStorage.removeItem('cvhome.console.store');
    api = new FakeUsersApi();
    toasts = {messages: [], danger(text: string) { this.messages.push(text); }};
    roles = {
      isSuperAdmin: false,
      isSupport: false,
      isOrgAdmin: true,
      isStoreAdmin: false,
      isStoreModerator: false,
    };
    await TestBed.configureTestingModule({
      imports: [Users, ...translocoTesting().imports],
      providers: [
        provideRouter([]),
        {provide: ConsoleApi, useValue: Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE})},
        {provide: UsersApi, useValue: api},
        {provide: NOTIFICATION_PORT, useValue: toasts},
        {
          provide: AuthService,
          useValue: {
            getCachedAuthUser: () => ({username: 'org1-store1-admin'}),
            getRoles: () => roles,
          },
        },
        ...translocoTesting().providers,
      ],
    }).compileComponents();
  });

  function load(): HTMLElement {
    fixture = TestBed.createComponent(Users);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  function settle(): void {
    tick();
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
  }

  function rowsOnScreen(host: HTMLElement): HTMLElement[] {
    return Array.from(host.querySelectorAll('app-table-row'));
  }

  it('lists the store team, one row each', fakeAsync(() => {
    const host = load();

    expect(rowsOnScreen(host).length).toBe(3);
    expect(host.textContent).toContain('org1-store1-admin Person');
    expect(host.textContent).toContain('priya@mail.com');
  }));

  /* `count`, not Spring's `size`. The whole platform renames it, and this is the page's end of that. */
  it('asks for a page sized by count', fakeAsync(() => {
    load();

    expect(api.requests[0].page).toEqual({page: 0, count: PAGE_SIZE});
  }));

  /*
   * The signed-in operator is matched on username, because the JWT `sub` is the username and there
   * is no id to compare. See lessons.md, "Users — the JWT carries no user id".
   */
  it('tells the operator which row is their own', fakeAsync(() => {
    const host = load();

    const own = rowsOnScreen(host)[0];
    expect(own.textContent).toContain('You');
    expect(rowsOnScreen(host)[1].textContent).not.toContain('You');
  }));

  /* A role name the console has words for is translated; the badge is not the raw enum. */
  it('renders roles as words, and says so when there are none', fakeAsync(() => {
    const host = load();

    expect(rowsOnScreen(host)[1].textContent).toContain('Store moderator');
    expect(rowsOnScreen(host)[2].textContent).toContain('—');
  }));

  it('shows an account that cannot sign in as disabled', fakeAsync(() => {
    const host = load();

    expect(rowsOnScreen(host)[1].textContent).toContain('Disabled');
    expect(rowsOnScreen(host)[0].textContent).toContain('Active');
  }));

  /*
   * The list is store-scoped and that genuinely hides people, so the page says so rather than
   * letting an operator conclude the team is smaller than it is.
   */
  it('names the scope the list is a reading of', fakeAsync(() => {
    const host = load();

    expect(host.querySelector('app-notice-bar')?.textContent).toContain('Acme');
  }));

  it('reports the failure and offers a retry rather than an empty team', fakeAsync(() => {
    api.failure = true;
    const host = load();

    expect(host.querySelector('app-load-error')?.textContent).toContain('the team could not be read');
    expect(rowsOnScreen(host).length).toBe(0);
  }));

  it('opens the rail on the person whose name was clicked', fakeAsync(() => {
    const host = load();

    (rowsOnScreen(host)[1].querySelector('.identity') as HTMLButtonElement).click();
    settle();

    const rail = host.querySelector('.rail') as HTMLElement;
    expect(rail.textContent).toContain('priya@mail.com');
    expect(rail.textContent).toContain('Store moderator');
  }));

  /*
   * Disabling yourself ends your own session on the next request and deleting yourself is worse.
   * The server stops neither, so the console does.
   */
  it('will not let an operator disable or delete their own account', fakeAsync(() => {
    const host = load();

    (rowsOnScreen(host)[0].querySelector('.identity') as HTMLButtonElement).click();
    settle();

    const destructive = Array.from(host.querySelectorAll('.rail button')).filter((button) =>
      /Block sign-in|Delete/.test(button.textContent ?? ''),
    ) as HTMLButtonElement[];

    expect(destructive.length).toBe(2);
    expect(destructive.every((button) => button.disabled)).toBeTrue();
  }));

  it('lets an operator act on somebody else', fakeAsync(() => {
    const host = load();

    (rowsOnScreen(host)[1].querySelector('.identity') as HTMLButtonElement).click();
    settle();

    const enable = Array.from(host.querySelectorAll('.rail button')).find((button) =>
      /Allow sign-in/.test(button.textContent ?? ''),
    ) as HTMLButtonElement;
    expect(enable.disabled).toBeFalse();

    enable.click();
    settle();

    expect(api.actives).toEqual([{userId: 'id-2', active: true}]);
  }));

  /*
   * A write re-reads rather than patching the row: the endpoints answer `void`, and what the
   * operator should see is what the server recorded.
   */
  it('re-reads the list after a write instead of flipping the row', fakeAsync(() => {
    const host = load();
    const before = api.requests.length;

    (rowsOnScreen(host)[1].querySelector('.identity') as HTMLButtonElement).click();
    settle();
    (Array.from(host.querySelectorAll('.rail button')).find((button) =>
      /Allow sign-in/.test(button.textContent ?? ''),
    ) as HTMLButtonElement).click();
    settle();

    expect(api.requests.length).toBeGreaterThan(before);
  }));

  /*
   * A moderator can read the team and change none of it — `USERS.LIST` resolves to the store's read
   * audience while every write resolves to `hasMaintainAccessOnUsers`. Rendering buttons certain to
   * 403 is worse than not rendering them.
   */
  it('offers a moderator the list and no write action at all', fakeAsync(() => {
    roles = {...roles, isOrgAdmin: false, isStoreModerator: true};
    const host = load();

    expect(rowsOnScreen(host).length).toBe(3);
    expect(host.textContent).not.toContain('Add user');
    expect(host.querySelectorAll('.cell-actions button').length).toBe(0);

    (rowsOnScreen(host)[1].querySelector('.identity') as HTMLButtonElement).click();
    settle();
    expect(host.querySelector('.rail')?.textContent).not.toContain('Set password');
  }));

  it('creates a user from the rail and reports it', fakeAsync(() => {
    const host = load();

    (Array.from(host.querySelectorAll('button')).find((button) =>
      /Add user/.test(button.textContent ?? ''),
    ) as HTMLButtonElement).click();
    settle();

    const form = host.querySelector('.rail-form') as HTMLFormElement;
    setControl(form, 'userName', 'newbie');
    setControl(form, 'emailAddress', 'newbie@mail.com');
    setControl(form, 'password', 'Passw0rd1');
    setControl(form, 'repeatPassword', 'Passw0rd1');
    (form.querySelector('app-checkbox input') as HTMLInputElement).click();
    settle();

    form.dispatchEvent(new Event('submit'));
    settle();

    expect(api.created.length).toBe(1);
    expect(api.created[0]).toEqual(
      jasmine.objectContaining({userName: 'newbie', emailAddress: 'newbie@mail.com', password: 'Passw0rd1'}),
    );
  }));

  /* At least one role is required — an account with none can sign in and do nothing. */
  it('refuses to submit a user with no role', fakeAsync(() => {
    const host = load();

    (Array.from(host.querySelectorAll('button')).find((button) =>
      /Add user/.test(button.textContent ?? ''),
    ) as HTMLButtonElement).click();
    settle();

    const form = host.querySelector('.rail-form') as HTMLFormElement;
    setControl(form, 'userName', 'newbie');
    setControl(form, 'emailAddress', 'newbie@mail.com');
    setControl(form, 'password', 'Passw0rd1');
    setControl(form, 'repeatPassword', 'Passw0rd1');
    settle();

    form.dispatchEvent(new Event('submit'));
    settle();

    expect(api.created.length).toBe(0);
  }));

  /*
   * The username is uaa's unique key and `updateUser` never carries a new one through, so an
   * editable field here would look like a rename and silently do nothing.
   */
  it('does not offer to rename an existing account', fakeAsync(() => {
    const host = load();

    (rowsOnScreen(host)[1].querySelector('.cell-actions button') as HTMLButtonElement).click();
    settle();

    const userName = host.querySelector('.rail-form [formControlName="userName"] input') as HTMLInputElement;
    expect(userName.disabled).toBeTrue();
  }));

  /* No password field on an edit: `PUT …/update` does not change one, and offering it would imply it did. */
  it('offers no password field when editing', fakeAsync(() => {
    const host = load();

    (rowsOnScreen(host)[1].querySelector('.cell-actions button') as HTMLButtonElement).click();
    settle();

    expect(host.querySelector('.rail-form [formControlName="password"]')).toBeNull();
  }));

  /*
   * The dialog collects the password twice and no current one: `UserPassword.password` is read by
   * nothing on the Java side. See lessons.md, "Users — no self-service password change".
   */
  it('sets a password from the dialog and never asks for the current one', fakeAsync(() => {
    const host = load();

    (rowsOnScreen(host)[1].querySelector('.identity') as HTMLButtonElement).click();
    settle();
    (Array.from(host.querySelectorAll('.rail button')).find((button) =>
      /Set password/.test(button.textContent ?? ''),
    ) as HTMLButtonElement).click();
    settle();

    const dialog = host.querySelector('app-set-password-dialog') as HTMLElement;
    const fields = Array.from(dialog.querySelectorAll('input')) as HTMLInputElement[];
    expect(fields.length).toBe(2);

    for (const field of fields) {
      field.value = 'Passw0rd1';
      field.dispatchEvent(new Event('input'));
    }
    settle();

    (dialog.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    settle();

    expect(api.resets).toEqual([{userId: 'id-2', password: 'Passw0rd1'}]);
  }));

  it('refuses a password the rule in the dialog rejects', fakeAsync(() => {
    const host = load();

    (rowsOnScreen(host)[1].querySelector('.identity') as HTMLButtonElement).click();
    settle();
    (Array.from(host.querySelectorAll('.rail button')).find((button) =>
      /Set password/.test(button.textContent ?? ''),
    ) as HTMLButtonElement).click();
    settle();

    const dialog = host.querySelector('app-set-password-dialog') as HTMLElement;
    const fields = Array.from(dialog.querySelectorAll('input')) as HTMLInputElement[];
    for (const field of fields) {
      field.value = 'short';
      field.dispatchEvent(new Event('input'));
    }
    settle();
    (dialog.querySelector('form') as HTMLFormElement).dispatchEvent(new Event('submit'));
    settle();

    expect(api.resets.length).toBe(0);
    expect(dialog.textContent).toContain('at least 8 characters');
  }));

  /* Two tiles, not the template's four — see the facade. Both read off the load that already happened. */
  it('reports the team size without a second request', fakeAsync(() => {
    const host = load();

    expect(api.requests.length).toBe(1);
    const kpis = host.querySelector('app-kpi-grid') as HTMLElement;
    expect(kpis.textContent).toContain('Team members');
    expect(kpis.textContent).toContain('3');
  }));

  /* Pending invitations are not known until that tab has loaded — an em dash, never a zero. */
  it('shows an em dash for a figure it has not earned', fakeAsync(() => {
    const host = load();

    const kpis = host.querySelector('app-kpi-grid') as HTMLElement;
    expect(kpis.textContent).toContain('Pending invitations');
    expect(kpis.textContent).toContain('—');
  }));

  it('keeps the previous rows on screen while the next page loads', fakeAsync(() => {
    const host = load();
    api.pending = new Subject<TeamSnapshot>();

    fixture.componentInstance['facade'].goToPage(1);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();

    expect(rowsOnScreen(host).length).toBe(3);
    api.resolve();
    settle();
  }));
});

function setControl(form: HTMLElement, name: string, value: string): void {
  const input = form.querySelector(`[formControlName="${name}"] input`) as HTMLInputElement;
  input.value = value;
  input.dispatchEvent(new Event('input'));
}
