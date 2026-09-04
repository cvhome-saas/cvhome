import {TestBed} from '@angular/core/testing';
import {Router} from '@angular/router';
import {Observable, of} from 'rxjs';
import {AuthService} from './auth.service';
import {CrudService} from '../http/crud.service';

/**
 * The one identity `/api/v1/auth/me` answers with that a person is ever shown.
 *
 * Two shapes reach it and they name the account differently. Through the gateway it is an OIDC
 * principal, whose `name` is the **subject** — and the subject is the account id now, because a
 * username is unique only within a realm and the identity layer keys sessions and authorizations on
 * it. Read straight into the toolbar, that is a UUID.
 */
describe('AuthService.getAuthUser', () => {

  function serving(body: unknown): AuthService {
    TestBed.configureTestingModule({
      providers: [
        {provide: CrudService, useValue: {get: (): Observable<unknown> => of(body)}},
        {provide: Router, useValue: {navigateByUrl: (): void => undefined}},
      ],
    });
    return TestBed.inject(AuthService);
  }

  const PRINCIPAL = {
    claims: {sub: '60ab49a5-7f06-4b5a-be81-9b30bb6559ae'},
    givenName: 'Ada',
    familyName: 'Lovelace',
    email: 'org1-admin@mail.com',
    preferredUsername: 'org1-admin',
    name: '60ab49a5-7f06-4b5a-be81-9b30bb6559ae',
  };

  it('shows the handle, not the subject, for a gateway principal', (done) => {
    serving({principal: PRINCIPAL, authorities: [{authority: 'ROLE_ORG_ADMIN'}]}).getAuthUser()
      .subscribe((user) => {
        expect(user.username).toBe('org1-admin');
        expect(user.sub).toBe('60ab49a5-7f06-4b5a-be81-9b30bb6559ae');
        expect(user.givenName).toBe('Ada');
        done();
      });
  });

  it('falls back to the subject when there is no handle, so a name is never blank', (done) => {
    serving({principal: {...PRINCIPAL, preferredUsername: null}, authorities: []}).getAuthUser()
      .subscribe((user) => {
        expect(user.username).toBe('60ab49a5-7f06-4b5a-be81-9b30bb6559ae');
        done();
      });
  });

  it("reads uaa's own session shape, which has no principal and no subject", (done) => {
    serving({username: 'org1-admin', firstName: 'Ada', roles: ['ORG_ADMIN'], authorities: []}).getAuthUser()
      .subscribe((user) => {
        expect(user.username).toBe('org1-admin');
        expect(user.sub).toBe('org1-admin');
        expect(user.roles).toEqual(['ORG_ADMIN']);
        done();
      });
  });

});
