import {NbCardModule, NbMenuService} from '@nebular/theme';
import { Component } from '@angular/core';
import {TranslateModule} from "@ngx-translate/core";

@Component({
  selector: 'ngx-not-found',
  styleUrls: ['./not-found.component.scss'],
  templateUrl: './not-found.component.html',
  imports:[NbCardModule,TranslateModule],
  standalone:true,
})
export class NotFoundComponent {

  constructor(private menuService: NbMenuService) {
  }

  goToHome() {
    this.menuService.navigateHome();
  }
}
