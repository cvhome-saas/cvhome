import {ChangeDetectorRef, Component, DestroyRef, EventEmitter, Input, Output, ViewChild, effect, inject} from '@angular/core';
import {ManagerStoreId} from '../../models/commons';
import {NbSelectComponent} from '@nebular/theme';
import {StoreAutocompleteFacade} from './facades/store-autocomplete.facade';

@Component({
  selector: 'ngx-store-autocomplete',
  standalone: false,
  templateUrl: './store-autocomplete.component.html',
  styleUrls: ['./store-autocomplete.component.scss'],
  providers: [StoreAutocompleteFacade]
})
export class StoreAutocompleteComponent {
  @ViewChild(NbSelectComponent)
  routeSelect: NbSelectComponent;
  @Output()
  onStore: EventEmitter<ManagerStoreId> = new EventEmitter<ManagerStoreId>();
  @Input()
  selectedItem: ManagerStoreId | undefined;
  @Input()
  selectable: boolean = true;

  protected readonly facade = inject(StoreAutocompleteFacade);
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly destroyRef = inject(DestroyRef);
  private lastStore: ManagerStoreId | undefined;

  constructor() {
    this.facade.init(this.destroyRef);

    effect(() => {
      const stores = this.facade.stores();
      if (stores.length > 0) {
        this.selectedItem = this.facade.resolveSelection(this.selectedItem);
        this.cdr.detectChanges();
        this.changed(this.selectedItem);
      }
    });
  }

  changed(event: ManagerStoreId | undefined): void {
    if (event != undefined && event.id != undefined && (this.lastStore == undefined || this.lastStore.id != event.id)) {
      this.onStore.emit(event);
    }
    this.lastStore = event;
  }
}
