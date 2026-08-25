import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';

import {AuthService} from '@core/auth/auth.service';
import {LocaleService} from '@core/i18n/locale.service';
import {ThemeService} from '@core/theme/theme.service';
import {translocoTesting} from '@testing/transloco-testing';
import {Profile} from './profile';

describe('Profile', () => {
  let fixture: ComponentFixture<Profile>;
  let authorities: string[];

  beforeEach(async () => {
    authorities = ['ROLE_ORG_ADMIN', 'SCOPE_store_core'];
    await TestBed.configureTestingModule({
      imports: [Profile, ...translocoTesting().imports],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            getCachedAuthUser: () => ({username: 'org1-admin', authorities}),
          },
        },
        ...translocoTesting().providers,
      ],
    }).compileComponents();
  });

  function load(): HTMLElement {
    fixture = TestBed.createComponent(Profile);
    fixture.detectChanges();
    tick();
    fixture.detectChanges();
    return fixture.nativeElement as HTMLElement;
  }

  it('shows the username, because it is the only identity the platform carries', fakeAsync(() => {
    const host = load();

    expect(host.textContent).toContain('org1-admin');
  }));

  /*
   * The roles come from the access token's `roles` claim, which JwtCustomizerConfig does populate —
   * so unlike the name and the email these are real. Scope authorities describe the client rather
   * than the person and are dropped.
   */
  it('names the roles and drops the scope authorities', fakeAsync(() => {
    const host = load();

    expect(host.textContent).toContain('Organization administrator');
    expect(host.textContent).not.toContain('store_core');
  }));

  /*
   * uaa's roles are a table rather than an enum, and Transloco throws on a missing key, so a role
   * added with POST /admin/roles must humanize instead of taking the page down.
   */
  it('renders a role the console has never seen without throwing', fakeAsync(() => {
    authorities = ['ROLE_REGIONAL_BUYER'];
    const host = load();

    expect(host.textContent).toContain('Regional Buyer');
  }));

  /*
   * Empty fields would read as "you have not filled these in". The page says why there is nothing
   * to fill in instead.
   */
  it('explains the missing name and email rather than showing blank fields', fakeAsync(() => {
    const host = load();

    expect(host.querySelector('app-notice-bar')?.textContent).toContain('only see your username');
    expect(host.textContent).not.toContain('Job title');
    expect(host.textContent).not.toContain('Bio');
  }));

  /* The two preferences the console genuinely owns, and the only interactive things on the page. */
  it('switches the language, and marks which one is active', fakeAsync(() => {
    const host = load();
    const locales = TestBed.inject(LocaleService);
    const select = spyOn(locales, 'select');

    const arabic = Array.from(host.querySelectorAll('.choice-option')).find((option) =>
      /العربية/.test(option.textContent ?? ''),
    ) as HTMLButtonElement;
    expect(arabic.getAttribute('aria-pressed')).toBe('false');

    arabic.click();
    tick();

    expect(select).toHaveBeenCalledWith('ar');
  }));

  it('switches the theme', fakeAsync(() => {
    const host = load();
    const themes = TestBed.inject(ThemeService);
    const select = spyOn(themes, 'select');

    const options = Array.from(host.querySelectorAll('.choice-option')) as HTMLButtonElement[];
    const theme = options[options.length - 1];
    theme.click();
    tick();

    expect(select).toHaveBeenCalled();
  }));

  /*
   * Both preferences are browser-local. Saying so is the difference between a preference and a
   * promise — see lessons.md, "Shell — no user-preferences endpoint".
   */
  it('says the preferences do not follow the operator to another device', fakeAsync(() => {
    const host = load();

    expect(host.textContent).toContain('this browser only');
  }));

  /*
   * Billing is not here at all. A plan belongs to a **store**, not to a person, so it lives with
   * the store in the nav rail rather than under an account page that cannot say whose it is.
   */
  it('does not draw billing on a personal page', fakeAsync(() => {
    const host = load();

    expect(host.textContent).not.toContain('Billing');
  }));

  /*
   * Setting a password needs a user id and the JWT carries a username, so the action lives on
   * /users where a row has a real id. See lessons.md, "Users — no self-service password change".
   */
  it('offers no password control, because there is no endpoint it could call', fakeAsync(() => {
    const host = load();

    expect(host.textContent).not.toContain('Password');
    expect(host.querySelector('input[type="password"]')).toBeNull();
  }));
});
