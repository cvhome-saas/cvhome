import {Component, effect, inject, input, computed} from '@angular/core';
import {Router} from '@angular/router';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

import type {AuthStory as AuthStoryModel} from '@cvhome-saas/ui-kit';
import {Icon} from '@cvhome-saas/ui-kit/ui';
import {AuthStory} from '../components/auth-story';
import {AcceptInvitationFacade} from './facades/accept-invitation.facade';

/**
 * Accepting an invitation to join an organization.
 *
 * **Under `features/auth/` rather than beside the console pages**, because it is an `AuthShell`
 * route and `AuthShell` is a bare `<router-outlet>` — every page on it brings its own frame, from
 * this feature's shared `auth.css`. Putting it anywhere else meant either a page with no chrome at
 * all or one feature reaching into another's stylesheet.
 *
 * It cannot sit inside the console shell: an invitee is authenticated and is not yet a member of
 * anything, so `consoleContext` and `requiresStore` would both refuse them. That is exactly why
 * `OrgMemberApi.accept` carries no permission token either — the bearer token in the link is the
 * authorization.
 *
 * TODO(lessons.md): an invitee with no cvhome account has no way through this page — see
 * lessons.md, "Users — an invitee needs an account before the link can work". The invite dialog
 * says so up front, which is the only place the console can.
 *
 * Accepting is a button rather than something that happens on load. The token is single-use and is
 * burned on the first success, so firing it from an effect would turn a refresh, a prefetch or a
 * link preview into "this invitation has already been used".
 */
@Component({
  selector: 'app-accept-invitation',
  imports: [AuthStory, Icon, TranslocoDirective],
  providers: [AcceptInvitationFacade],
  templateUrl: './accept-invitation.html',
  styleUrls: ['../auth.css', './accept-invitation.css'],
})
export class AcceptInvitation {
  private readonly router = inject(Router);
  private readonly transloco = inject(TranslocoService);

  protected readonly facade = inject(AcceptInvitationFacade);

  /** The invitation token, from `?token=`, bound by `withComponentInputBinding()`. */
  readonly token = input<string>();

  /** The same panel sign-in and sign-up carry, so the three pages read as one flow. */
  protected readonly story = computed<AuthStoryModel>(() => {
    this.transloco.activeLang();
    return {
      heading: this.transloco.translate('acceptInvitation.story.heading'),
      copy: this.transloco.translate('acceptInvitation.story.copy'),
      points: this.transloco.translate<string[]>([
        'acceptInvitation.story.point1',
        'acceptInvitation.story.point2',
        'acceptInvitation.story.point3',
      ]),
    };
  });

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
