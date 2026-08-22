import {Component, effect, inject, input} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoDirective} from '@jsverse/transloco';

import {Icon} from '@shared/ui/icon/icon';
import {LoadError} from '@shared/ui/load-error/load-error';
import {AcceptInvitationFacade} from './facades/accept-invitation.facade';

/**
 * Accepting an invitation to join an organization.
 *
 * Outside the console shell, because an invitee is authenticated and is not yet a member of
 * anything: `consoleContext` resolves the store list and `requiresStore` insists on one, and this
 * person has neither. `OrgMemberApi.accept` makes the same call — it is the one method on that
 * controller with no permission token, because no org-scoped check could pass for someone who is not
 * in the org yet. The bearer token in the link is the authorization.
 *
 * Accepting is a button rather than something that happens on load. The token is single-use and is
 * burned on the first success, so firing it from an effect would turn a refresh, a prefetch or a
 * link preview into "this invitation has already been used".
 */
@Component({
  selector: 'app-accept-invitation',
  imports: [Icon, LoadError, TranslocoDirective],
  providers: [AcceptInvitationFacade],
  templateUrl: './accept-invitation.html',
  styleUrl: './accept-invitation.css',
})
export class AcceptInvitation {
  private readonly router = inject(Router);

  protected readonly facade = inject(AcceptInvitationFacade);

  /** The invitation token, from `?token=`, bound by `withComponentInputBinding()`. */
  readonly token = input<string>();

  constructor() {
    // Nothing is sent here — this only decides whether there is anything to send at all.
    effect(() => {
      if (!this.token()) {
        this.facade.state.set('refused');
        this.facade.failure.set(null);
      }
    });
  }

  protected accept(): void {
    const token = this.token();
    if (token) {
      this.facade.accept(token);
    }
  }

  protected enterConsole(): void {
    void this.router.navigate(['/dashboard']);
  }
}
