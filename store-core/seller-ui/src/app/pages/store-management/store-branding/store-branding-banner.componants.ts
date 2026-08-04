import {Component, Input, OnInit, ViewChild, ElementRef, inject} from "@angular/core";
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbInputModule, NbSpinnerModule} from '@nebular/theme';
import {StoreBrandingBannerFacade} from "./facades/store-branding-banner.facade";
import {ReadableMerchantStoreWithPod} from '../models/store-service.model';

@Component({
  selector: 'ngx-store-branding-banner',
  standalone: true,
  imports: [ReactiveFormsModule, TranslateModule, NbButtonModule, NbInputModule, NbSpinnerModule],
  template: `
    <form [formGroup]="facade.imageUpload">
      <div (click)='imageInput.click()' (dragover)="facade.allowDrop($event)" (drop)="facade.drop($event, imageDrop)"
           (keydown.enter)='imageInput.click()' class="wrapper" role="button" tabindex="0">
        <div #imageDrop id="imageDrop">
          @if (facade.banner()) {
            <img [src]="facade.banner()?.path" alt="{{ 'STORE_BRANDING.BANNER' | translate }}" style="height:250px"/>
          }
        </div>
        <div class="uploadingPhrase">{{ 'STORE_BRANDING.DROP_FILE' | translate }}</div>
      </div>

      <input nbInput fullWidth #imageInput (change)='facade.imageChange($event, imageDrop)' formControlName="imageInput" id="imageInput" required
             type="file">

      <div class="form-group actions-button">
        @if (facade.showRemoveButton()) {
          <button nbButton status="danger" (click)="facade.removeBanner(store.id, imageDrop)" type="submit">{{
              'COMMON.REMOVE' | translate
            }}
          </button>
        }
        @if (facade.bannerFile) {
          <button nbButton status="success" (click)="facade.saveBanner(store.id)" [nbSpinner]="facade.loadingButton()"
                  nbSpinnerMessage="" nbSpinnerSize="large" type="submit">
            {{ (!facade.loadingButton() ? 'COMMON.SAVE' : '') | translate }}
          </button>
        }
      </div>
    </form>
  `,
  styleUrls: ['./store-branding.component.scss'],
  providers: [StoreBrandingBannerFacade]
})
export class StoreBrandingBannerComponent implements OnInit {
  @Input() store: ReadableMerchantStoreWithPod;
  @ViewChild('imageDrop', {static: false}) imageDrop!: ElementRef;

  protected readonly facade = inject(StoreBrandingBannerFacade);

  ngOnInit(): void {
    this.facade.init(this.store);
  }
}
