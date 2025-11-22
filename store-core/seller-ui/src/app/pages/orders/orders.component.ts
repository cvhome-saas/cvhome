import {Component, OnInit} from '@angular/core';
import {SelectedLanguageService} from '../shared/services/selected-language.service';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'ngx-orders',
  standalone: false,
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit {

  constructor(
    private selectedLanguageService: SelectedLanguageService,
    private translateService: TranslateService,
  ) {
    this.translateService.use(this.selectedLanguageService.current());
  }

  ngOnInit() {
  }
}
