import {Component, inject} from '@angular/core';
import {TranslocoDirective} from '@jsverse/transloco';

import {Badge} from '@shared/ui/badge/badge';
import {Icon} from '@shared/ui/icon/icon';
import {NoticeBar} from '@shared/ui/notice-bar/notice-bar';
import {PageHeader} from '@shared/ui/page-header/page-header';
import {Panel} from '@shared/ui/panel/panel';
import {ProfileFacade} from './facades/profile.facade';

/**
 * The signed-in operator's own account.
 *
 * Small, and honestly so. `Account Profile.dc.html` designs six tabs; three name a subject this
 * console can reach and two of those have no data behind them at all. What is built is the part that
 * is true: who you are signed in as, what you may do, and the two preferences the console owns.
 *
 * Removed, each with a `lessons.md` entry: the avatar, full name, job title, email, phone, timezone,
 * date format and bio — none has a column anywhere, and the account record they would come from is
 * unreachable twice over. The notification toggles, two-factor and the session list have no service.
 * The Organization tab needs `org-manager/find-one`, which is super-admin only. Billing is not here
 * at all: a plan belongs to a **store**, not to a person, so it lives with the store in the nav rail
 * rather than under an account page that cannot show whose it is.
 *
 * The password card is not here either: setting one needs a user id, and the JWT carries the
 * username instead. That action lives on `/users`, where a row has a real id — which is also the
 * only place it makes sense, since the only password endpoint is an administrative reset.
 */
@Component({
  selector: 'app-profile',
  imports: [Badge, Icon, NoticeBar, PageHeader, Panel, TranslocoDirective],
  providers: [ProfileFacade],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile {
  protected readonly facade = inject(ProfileFacade);
}
