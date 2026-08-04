import {NbCardModule, NbMenuService} from '@nebular/theme';
import {Component, inject} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';

@Component({
  selector: 'ngx-not-found',
  standalone: true,
  imports: [TranslateModule, NbCardModule],
  styleUrls: ['./not-found.component.scss'],
  templateUrl: './not-found.component.html',
})
export class NotFoundComponent {
  private readonly menuService = inject(NbMenuService);

  goToHome() {
    this.menuService.navigateHome();
  }
}
