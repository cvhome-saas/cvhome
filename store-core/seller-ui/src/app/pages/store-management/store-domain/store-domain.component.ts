import {Component, OnInit, inject} from '@angular/core';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {StoreDomainFacade} from './facades/store-domain.facade';
import {StoreDomainFormService} from './services/store-domain.form.service';

@Component({
  selector: 'ngx-store-domain',
  standalone: false,
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
