import {Component} from '@angular/core';
import {TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {RouterTestingHarness} from '@angular/router/testing';

import {SubscriptionService} from '@api/billing/subscription.service';
import {
  FakeSubscriptionService,
  activeSubscription,
  trialingSubscription,
} from '@testing/billing.fake';
import {translocoTesting} from '@testing/transloco-testing';
import {CONSOLE_STORES_FAKE, FakeConsoleApi} from '@testing/console-api.fake';
import {ConsoleShell} from './console-shell';
import {ConsoleApi} from './services/console.api.service';
import {ConsoleShellFacade} from './facades/console-shell.facade';

@Component({selector: 'app-test-page', template: `<p class="test-page">{{ 'Inventory page' }}</p>`})
class TestPage {}

describe('ConsoleShell', () => {
  let billing: FakeSubscriptionService;
  let api: FakeConsoleApi;

  beforeEach(async () => {
    billing = new FakeSubscriptionService();
    api = Object.assign(new FakeConsoleApi(), {stores: CONSOLE_STORES_FAKE});
    await TestBed.configureTestingModule({
      imports: [ConsoleShell, ...translocoTesting().imports],
      providers: [
        // The shell mounts the plan banner, which reads billing.
        {provide: SubscriptionService, useValue: billing},
        provideRouter([]),
        ...translocoTesting().providers,
        {provide: ConsoleApi, useValue: api},
      ],
    }).compileComponents();
  });

  function shell() {
    const fixture = TestBed.createComponent(ConsoleShell);
    fixture.detectChanges();
    return {fixture, element: fixture.nativeElement as HTMLElement};
  }

  it('renders the chrome and an outlet for pages', () => {
    const {element} = shell();

    // The plan banner is deliberately absent here: it is conditional on billing having something to
    // say, and this store's subscription is healthy. See `dismisses the plan banner` below.
    expect(element.querySelector('app-console-sidebar')).not.toBeNull();
    expect(element.querySelector('app-console-toolbar')).not.toBeNull();
    expect(element.querySelector('router-outlet')).not.toBeNull();
  });

  it('opens top navigation popovers without clipping them', () => {
    const {fixture, element} = shell();

    (element.querySelector('.profile-toggle') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(element.querySelector('.profile-menu')).not.toBeNull();
    expect(getComputedStyle(element.querySelector('.toolbar') as HTMLElement).overflow).not.toBe(
      'hidden',
    );
  });

  it('opens the language switcher', () => {
    const {fixture, element} = shell();

    (element.querySelector('.language-toggle') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(element.querySelector('.language-menu')).not.toBeNull();
  });

  it('keeps only one menu open at a time', () => {
    const {fixture, element} = shell();

    (element.querySelector('.profile-toggle') as HTMLButtonElement).click();
    fixture.detectChanges();
    (element.querySelector('.language-toggle') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(element.querySelector('.profile-menu')).toBeNull();
    expect(element.querySelector('.language-menu')).not.toBeNull();
  });

  it('shows no plan banner for a store in good standing', fakeAsync(() => {
    billing.subscription = activeSubscription();
    const {fixture, element} = shell();
    tick();
    fixture.detectChanges();

    // The banner used to tell a paying customer to upgrade, on every page. Nothing to say is now the
    // common case and renders nothing at all — including the row it used to reserve.
    expect(element.querySelector('app-plan-banner')).toBeNull();
    expect(element.querySelector('.console')?.classList).not.toContain('banner-on');
  }));

  it('dismisses the plan banner', fakeAsync(() => {
    billing.subscription = trialingSubscription();
    const {fixture, element} = shell();
    tick();
    fixture.detectChanges();

    expect(element.querySelector('app-plan-banner')).not.toBeNull();
    (element.querySelector('.dismiss') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(element.querySelector('app-plan-banner')).toBeNull();
  }));

  it('hosts any child route inside its workspace, with the chrome supplied', async () => {
    TestBed.resetTestingModule();
    await TestBed.configureTestingModule({
      imports: [...translocoTesting().imports],
      providers: [
        // The shell mounts the plan banner, which reads billing.
        {provide: SubscriptionService, useClass: FakeSubscriptionService},
        provideRouter([
          {
            path: 'inventory',
            component: ConsoleShell,
            children: [{path: '', component: TestPage, data: {breadcrumbKey: 'shell.breadcrumb.orders'}}],
          },
        ]),
        ...translocoTesting().providers,
        {provide: ConsoleApi, useValue: api},
      ],
    }).compileComponents();

    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/inventory');
    harness.detectChanges();
    const element = harness.routeNativeElement as HTMLElement;

    // The page renders in the shell's workspace and inherits the chrome for free.
    expect(element.querySelector('.workspace .test-page')).not.toBeNull();
    expect(element.querySelector('app-console-sidebar')).not.toBeNull();
    expect(element.querySelector('app-console-toolbar')).not.toBeNull();
    // And the toolbar names it from the route, with no coupling to the page.
    expect(element.querySelector('.breadcrumb strong')?.textContent?.trim()).toBe('Orders');
  });

  it('collapses the navigation rail on a wide viewport', () => {
    const {fixture, element} = shell();
    // Pinned rather than inherited from the Karma window, which is narrow.
    TestBed.inject(ConsoleShellFacade).isCompact.set(false);
    fixture.detectChanges();

    expect(element.querySelector('.console-body.nav-collapsed')).toBeNull();
    (element.querySelector('.nav-toggle') as HTMLButtonElement).click();
    fixture.detectChanges();

    expect(element.querySelector('.console-body.nav-collapsed')).not.toBeNull();
  });

  it('turns the rail toggle into a drawer dismiss when compact', () => {
    const {fixture, element} = shell();
    const facade = TestBed.inject(ConsoleShellFacade);

    facade.isCompact.set(true);
    facade.mobileNavOpen.set(true);
    fixture.detectChanges();

    const toggle = element.querySelector('.nav-toggle') as HTMLButtonElement;
    expect(toggle.getAttribute('aria-label')).toBe('Close navigation');

    toggle.click();
    fixture.detectChanges();

    expect(facade.mobileNavOpen()).toBeFalse();
    // Collapsing is meaningless at this size, so it must not be left behind as state.
    expect(facade.navCollapsed()).toBeFalse();
    expect(element.querySelector('.console-body.nav-collapsed')).toBeNull();
  });

  it('closes the drawer on Escape', () => {
    const {fixture} = shell();
    const facade = TestBed.inject(ConsoleShellFacade);

    facade.isCompact.set(true);
    facade.mobileNavOpen.set(true);
    fixture.detectChanges();

    document.dispatchEvent(new KeyboardEvent('keydown', {key: 'Escape'}));
    fixture.detectChanges();

    expect(facade.mobileNavOpen()).toBeFalse();
  });

  it('drops the open drawer when the viewport grows back', () => {
    const {fixture} = shell();
    const facade = TestBed.inject(ConsoleShellFacade);

    facade.isCompact.set(true);
    facade.mobileNavOpen.set(true);
    fixture.detectChanges();

    facade.isCompact.set(false);
    fixture.detectChanges();

    expect(facade.mobileNavOpen()).toBeFalse();
  });

  /*
   * The two products this application is. The rail is the only place they meet, and what keeps them
   * apart is each group's audience — see `.agents/plans/seller-ui-feature-inventory.md` §1.
   *
   * It has to cut **both** ways. The platform group is super-admin only, so a merchant seeing it
   * would get four screens that 403; and the merchant groups are each a reading of one store, which
   * a platform operator does not have — their store list is a truncated page of every tenant's and
   * the switcher is hidden, so those pages 403 too. Showing either set to the wrong operator was
   * showing pages that fail.
   */
  describe('the rail’s two audiences', () => {
    /** The nav the shell is actually given, with the platform group added to the fake's merchant ones. */
    function withPlatformGroup(): void {
      api.navigation = [
        ...api.navigation,
        {
          groupKey: 'shell.nav.group.platform',
          audience: 'platform',
          items: [{labelKey: 'shell.nav.item.organizations', icon: 'building', route: '/platform/organizations'}],
        },
      ];
    }

    it('keeps the platform group out of a merchant’s rail', () => {
      withPlatformGroup();
      const {element} = shell();

      expect(element.querySelector('a[href="/platform/organizations"]')).toBeNull();
    });

    it('gives a platform operator the platform group', () => {
      withPlatformGroup();
      api.platformOperator = true;
      const {element} = shell();

      expect(element.querySelector('a[href="/platform/organizations"]')).not.toBeNull();
    });

    it('leaves a merchant their own groups', () => {
      withPlatformGroup();
      const {element} = shell();

      expect(element.querySelector('a[href="/dashboard"]')).not.toBeNull();
      expect(element.querySelector('a[href="/store-management"]')).not.toBeNull();
    });

    /*
     * The half the user reported: a super admin's Home page was an access-denied banner, because
     * every merchant page reads a store and the one they are handed belongs to someone else.
     */
    it('keeps the merchant groups out of a platform operator’s rail', () => {
      withPlatformGroup();
      api.platformOperator = true;
      const {element} = shell();

      expect(element.querySelector('a[href="/dashboard"]')).toBeNull();
      expect(element.querySelector('a[href="/store-management"]')).toBeNull();
    });

    /*
     * `InternalStoreServiceImpl.findAll` reads a null org claim as platform-wide, so a super admin's
     * "your stores" is a truncated page of every tenant's. Offering it as a picker hands them a
     * stranger's shop. See lessons.md, "Shell — a super admin's store rail is the whole platform,
     * truncated".
     */
    it('shows the store switcher to a merchant', () => {
      const {element} = shell();

      expect(element.querySelector('app-store-switcher')).not.toBeNull();
    });

    it('hides the store switcher from a platform operator, whose store list is not their own', () => {
      api.platformOperator = true;
      const {element} = shell();

      expect(element.querySelector('app-store-switcher')).toBeNull();
    });
  });
});
