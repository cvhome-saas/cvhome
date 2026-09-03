import {ComponentFixture, TestBed} from '@angular/core/testing';

import {kitTranslocoTesting} from '@cvhome-saas/ui-kit/i18n';
import {UserAdminTable} from './user-admin-table';
import type {PlatformUserRow} from '../user-row';

/**
 * Which actions a row offers, and why that is the host's decision.
 *
 * The platform console may reset a password and assign roles. A merchant administering their own
 * store's shoppers may not: those accounts self-register, their roles are the deployment's
 * configuration, and cua exposes no endpoint for either — so a menu entry would answer 404.
 */
describe('UserAdminTable.allow', () => {
  const ROW: PlatformUserRow = {
    id: 'u-1',
    username: 'mia',
    email: 'mia@example.com',
    name: 'Mia Example',
    roles: [],
    enabled: true,
    status: 'ACTIVE',
    lastSignInAt: null,
    org: null,
    store: null,
    initials: 'ME',
  };

  let fixture: ComponentFixture<UserAdminTable>;

  beforeEach(async () => {
    const transloco = kitTranslocoTesting();
    await TestBed.configureTestingModule({
      imports: [UserAdminTable, ...(transloco.imports as never[])],
      providers: transloco.providers,
    }).compileComponents();
    fixture = TestBed.createComponent(UserAdminTable);
    fixture.componentRef.setInput('rows', [ROW]);
    fixture.componentRef.setInput('label', 'accounts');
    fixture.componentRef.setInput('roleList', () => '');
  });

  function keys(allow?: readonly string[]): string[] {
    if (allow) {
      fixture.componentRef.setInput('allow', allow);
    }
    fixture.detectChanges();
    const table = fixture.componentInstance as unknown as {
      actionsFor(row: PlatformUserRow): {key: string}[];
    };
    return table.actionsFor(ROW).map((action) => action.key);
  }

  it('offers everything by default, so the platform console is unchanged', () => {
    expect(keys()).toEqual(jasmine.arrayContaining(['resetPassword', 'editRoles', 'delete']));
  });

  it('offers only what the host says it can do', () => {
    const offered = keys(['toggleEnabled', 'delete']);

    expect(offered).toEqual(jasmine.arrayContaining(['toggleEnabled', 'delete']));
    expect(offered).not.toContain('resetPassword');
    expect(offered).not.toContain('editRoles');
  });

  /** Listed and disabled rather than omitted: a capability the product intends to have. */
  it('keeps impersonate visible whatever the host allows', () => {
    expect(keys(['delete'])).toContain('impersonate');
  });
});
