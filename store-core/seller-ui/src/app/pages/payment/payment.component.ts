import {Component, OnInit} from '@angular/core';
import {SelectedLanguageService} from '../shared/services/selected-language.service';
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'ngx-payment',
  standalone: false,
  templateUrl: './payment.component.html',
  styleUrls: ['./payment.component.scss']
})
export class PaymentComponent implements OnInit {

  constructor(
    private selectedLanguageService: SelectedLanguageService,
    private translateService: TranslateService,
  ) {
    this.translateService.use(this.selectedLanguageService.current());
  }

  ngOnInit() {
  }
}
