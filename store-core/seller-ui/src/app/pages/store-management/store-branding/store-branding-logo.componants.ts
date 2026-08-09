import {Component, Input, OnInit, ViewChild, ElementRef, inject} from "@angular/core";
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {NbButtonModule, NbInputModule, NbSpinnerModule} from '@nebular/theme';
import {StoreBrandingLogoFacade} from "./facades/store-branding-logo.facade";
import {ReadableMerchantStoreWithPod} from 'seller-core/stores';

@Component({
  selector: 'ngx-store-branding-logo',
  standalone: true,
  imports: [ReactiveFormsModule, TranslateModule, NbButtonModule, NbInputModule, NbSpinnerModule],
  template: `
    <form [formGroup]="facade.imageUpload">
      <div (click)='imageInput.click()' (dragover)="facade.allowDrop($event)" (drop)="facade.drop($event, imageDrop)"
           (keydown.enter)='imageInput.click()' class="wrapper" role="button" tabindex="0">
        <div #imageDrop id="imageDrop">
          @if (facade.logo()) {
            <img [src]="facade.logo()?.path" alt="{{ 'STORE_BRANDING.LOGO' | translate }}" style="height:250px"/>
          }
        </div>
        <div class="uploadingPhrase">{{ 'STORE_BRANDING.DROP_FILE' | translate }}</div>
      </div>

      <input nbInput fullWidth #imageInput (change)='facade.imageChange($event, imageDrop)' formControlName="imageInput" id="imageInput" required
             type="file">

      <div class="form-group actions-button">
        @if (facade.showRemoveButton()) {
          <button nbButton status="danger" (click)="facade.removeLogo(store.id, imageDrop)" type="submit">{{
              'COMMON.REMOVE' | translate
            }}
          </button>
        }
        @if (facade.logoFile) {
          <button nbButton status="success" (click)="facade.saveLogo(store.id)" [nbSpinner]="facade.loadingButton()"
                  nbSpinnerMessage="" nbSpinnerSize="large" type="submit">
            {{ (!facade.loadingButton() ? 'COMMON.SAVE' : '') | translate }}
          </button>
        }
      </div>
    </form>
  `,
  styleUrls: ['./store-branding.component.scss'],
  providers: [StoreBrandingLogoFacade]
})
export class StoreBrandingLogoComponent implements OnInit {
  @Input() store: ReadableMerchantStoreWithPod;
  @ViewChild('imageDrop', {static: false}) imageDrop!: ElementRef;

  protected readonly facade = inject(StoreBrandingLogoFacade);

  ngOnInit(): void {
    this.facade.init(this.store);
  }
}
