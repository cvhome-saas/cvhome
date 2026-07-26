import {Component, Input, OnChanges, OnInit, SimpleChanges, inject} from '@angular/core';
import {StoreFormFacade} from './facades/store-form.facade';
import {StoreFormService} from './services/store-form.service';

@Component({
  selector: 'ngx-store-form',
  standalone: false,
  templateUrl: './store-form.component.html',
  styleUrls: ['./store-form.component.scss'],
  providers: [StoreFormFacade, StoreFormService]
})
export class StoreFormComponent implements OnInit, OnChanges {
  @Input() title: string;
  @Input() store: any;
  @Input() isCancel: string;

  protected readonly facade = inject(StoreFormFacade);

  ngOnInit() {
    this.facade.init(this.store);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['store'] && !changes['store'].isFirstChange()) {
      this.facade.setStore(this.store);
    }
  }
}
