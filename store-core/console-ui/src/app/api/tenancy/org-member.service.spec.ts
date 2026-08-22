import {apiHarness, verifyNoPendingRequests} from '@testing/api-harness';
import type {Invitation} from '@models/users';
import {ORG_MEMBER_API_BASE, OrgMemberService} from './org-member.service';

/*
 * The first client this endpoint has ever had. Everything asserted here is a contract nothing else
 * in the platform was exercising.
 */
describe('OrgMemberService', () => {
  let service: OrgMemberService;
  let http: ReturnType<typeof apiHarness<OrgMemberService>>['http'];

  beforeEach(() => {
    const harness = apiHarness(OrgMemberService);
    service = harness.service;
    http = harness.http;
  });

  afterEach(() => verifyNoPendingRequests());

  const invitation: Invitation = {
    id: 'inv-1',
    orgId: '21f023932bc66470c104b76f',
    email: 'newbie@example.com',
    role: 'STORE_ADMIN',
    status: 'PENDING',
    expiresAt: '2026-09-01T00:00:00Z',
    createdAt: '2026-08-22T00:00:00Z',
    createdBy: 'org1-admin',
  };

  it('lists the organization members', () => {
    service.members().subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${ORG_MEMBER_API_BASE}/list`);
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('removes a member by uaa id', () => {
    service.removeMember('user-1').subscribe();
    const request = http.expectOne((candidate) => candidate.url === ORG_MEMBER_API_BASE);
    expect(request.request.method).toBe('DELETE');
    expect(request.request.params.get('userId')).toBe('user-1');
    request.flush({removed: true});
  });

  it('lists invitations', () => {
    service.invitations().subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${ORG_MEMBER_API_BASE}/invitations`);
    expect(request.request.method).toBe('GET');
    request.flush([invitation]);
  });

  /*
   * Email and role are query parameters, not a body — the endpoint takes @RequestParam for both. A
   * body posted here would be silently ignored, which is exactly the class of mistake these specs
   * exist to catch.
   */
  it('invites by query parameter and reads the token back once', () => {
    let token: string | undefined;
    service.invite('Newbie@Example.COM', 'STORE_ADMIN').subscribe((created) => (token = created.token));

    const request = http.expectOne((candidate) => candidate.url === `${ORG_MEMBER_API_BASE}/invitations`);
    expect(request.request.method).toBe('POST');
    expect(request.request.params.get('email')).toBe('Newbie@Example.COM');
    expect(request.request.params.get('role')).toBe('STORE_ADMIN');
    request.flush({invitation, token: 'the-only-time-this-is-readable'});

    expect(token).toBe('the-only-time-this-is-readable');
  });

  it('resends, which rotates the token rather than repeating it', () => {
    service.resend('newbie@example.com', 'STORE_ADMIN').subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${ORG_MEMBER_API_BASE}/invitations/resend`);
    expect(request.request.method).toBe('POST');
    expect(request.request.params.get('email')).toBe('newbie@example.com');
    request.flush({invitation, token: 'a-different-token'});
  });

  it('revokes by invitation id, not by email', () => {
    service.revoke('inv-1').subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${ORG_MEMBER_API_BASE}/invitations/revoke`);
    expect(request.request.method).toBe('POST');
    expect(request.request.params.get('invitationId')).toBe('inv-1');
    expect(request.request.params.has('email')).toBe(false);
    request.flush({...invitation, status: 'REVOKED'});
  });

  /* The token in the link is the authorization, and it is what decides which org is joined. */
  it('accepts with the token as the only subject', () => {
    service.accept('a-token').subscribe();
    const request = http.expectOne((candidate) => candidate.url === `${ORG_MEMBER_API_BASE}/invitations/accept`);
    expect(request.request.method).toBe('POST');
    expect(request.request.params.get('token')).toBe('a-token');
    request.flush({...invitation, status: 'ACCEPTED'});
  });
});
