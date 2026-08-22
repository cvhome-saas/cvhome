import {Injectable, computed, inject, signal} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';

import {OrgMemberService} from '@api/tenancy/org-member.service';
import {ApiErrorService} from '@core/errors/api-error.service';

/** What the page is doing. */
export type AcceptState = 'idle' | 'working' | 'joined' | 'refused';

/**
 * Accepting an invitation.
 *
 * The smallest facade in the console, and deliberately not a `snapshot()`: accepting is a **write**,
 * and a resource keyed on the token would fire it again on any re-render. `POST …/invitations/accept`
 * is single-use — the token is burned on the first success — so an accidental second call is not
 * harmless, it is the difference between "you have joined" and "this link has already been used".
 * The operator presses a button, once.
 */
@Injectable()
export class AcceptInvitationFacade {
  private readonly members = inject(OrgMemberService);
  private readonly apiErrors = inject(ApiErrorService);
  private readonly transloco = inject(TranslocoService);

  readonly state = signal<AcceptState>('idle');
  readonly organization = signal<string | null>(null);
  readonly failure = signal<string | null>(null);

  readonly busy = computed(() => this.state() === 'working');

  accept(token: string): void {
    if (this.busy() || this.state() === 'joined') {
      return;
    }
    this.state.set('working');
    this.failure.set(null);
    this.members.accept(token).subscribe({
      next: (invitation) => {
        this.state.set('joined');
        this.organization.set(invitation.orgId);
      },
      error: (error: unknown) => {
        this.state.set('refused');
        /*
         * Rendered on the page rather than raised as a toast. A toast dismisses itself, and this is
         * the entire content of the screen — an invitee who has just been told "this link no longer
         * works" needs it to stay on the page while they go and ask for a new one.
         */
        this.failure.set(this.apiErrors.messageFor(error));
      },
    });
  }

  heading(): string {
    this.transloco.activeLang();
    return this.transloco.translate('acceptInvitation.title');
  }
}
