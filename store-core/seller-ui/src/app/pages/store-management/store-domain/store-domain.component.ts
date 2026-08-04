import {Component, OnInit, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {ColumnMode, NgxDatatableModule} from "@swimlane/ngx-datatable";
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbIconModule, NbInputModule, NbOptionModule, NbSelectModule} from '@nebular/theme';
import {StoreDomainFacade} from './facades/store-domain.facade';
import {StoreDomainFormService} from './services/store-domain.form.service';

@Component({
  selector: 'ngx-store-domain',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbIconModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule,
    NgxDatatableModule
  ],
  templateUrl: './store-domain.component.html',
  styleUrls: ['./store-domain.component.scss'],
  providers: [StoreDomainFacade, StoreDomainFormService]
})
export class StoreDomainComponent implements OnInit {
  protected readonly ColumnMode = ColumnMode;
  protected readonly facade = inject(StoreDomainFacade);

  ngOnInit() {
    this.facade.init();
  }
}
