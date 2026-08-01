import {Component} from '@angular/core';
import {NbCardModule} from '@nebular/theme';
import {TranslateModule} from '@ngx-translate/core';

@Component({
  selector: 'ngx-usage',
  standalone: true,
  imports: [NbCardModule, TranslateModule],
  templateUrl: './usage.component.html',
  styleUrls: ['./usage.component.scss']
})
export class UsageComponent {
}
