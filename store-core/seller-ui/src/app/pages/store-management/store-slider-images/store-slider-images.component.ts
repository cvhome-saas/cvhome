import {Component, OnInit, inject} from '@angular/core';
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbCardModule, NbOptionModule, NbSelectModule} from '@nebular/theme';
import {StoreSliderImagesFacade, SliderImage} from './facades/store-slider-images.facade';

export type { SliderImage };

@Component({
  selector: 'ngx-store-slider-images',
  standalone: true,
  imports: [TranslateModule, NbButtonModule, NbCardModule, NbOptionModule, NbSelectModule],
  templateUrl: './store-slider-images.component.html',
  styleUrls: ['./store-slider-images.component.scss'],
  providers: [StoreSliderImagesFacade]
})
export class StoreSliderImagesComponent implements OnInit {
  protected readonly facade = inject(StoreSliderImagesFacade);

  ngOnInit() {
    this.facade.init();
  }
}
