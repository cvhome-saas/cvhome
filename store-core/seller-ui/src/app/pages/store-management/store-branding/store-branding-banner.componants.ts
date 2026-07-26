import {Component, Input, OnInit, ViewChild, ElementRef, inject} from "@angular/core";
import {StoreBrandingBannerFacade} from "./facades/store-branding-banner.facade";

@Component({
  selector: 'ngx-store-branding-banner',
  standalone: false,
  template: `
    <form [formGroup]="facade.imageUpload">
      <div (click)='imageInput.click()' (dragover)="facade.allowDrop($event)" (drop)="facade.drop($event, imageDrop)" class="wrapper">
        <div #imageDrop id="imageDrop">
          <img *ngIf="facade.banner()" [src]="facade.banner()?.path" style="height:250px"/>
        </div>
        <div class="uploadingPhrase">{{ 'STORE_BRANDING.DROP_FILE' | translate }}</div>
      </div>

      <input nbInput fullWidth #imageInput (change)='facade.imageChange($event, imageDrop)' formControlName="imageInput" id="imageInput" required
             type="file">

      <div class="form-group actions-button">
        <button nbButton status="danger" (click)="facade.removeBanner(store.id, imageDrop)" *ngIf="facade.showRemoveButton()" type="submit">{{
            'COMMON.REMOVE' | translate
          }}
        </button>
        <button nbButton status="success" (click)="facade.saveBanner(store.id)" *ngIf="facade.bannerFile" [nbSpinner]="facade.loadingButton()"
                nbSpinnerMessage="" nbSpinnerSize="large" type="submit">
          {{ (!facade.loadingButton() ? 'COMMON.SAVE' : '') | translate }}
        </button>
      </div>
    </form>
  `,
  styleUrls: ['./store-branding.component.scss'],
  providers: [StoreBrandingBannerFacade]
})
export class StoreBrandingBannerComponent implements OnInit {
  @Input() store: any;
  @ViewChild('imageDrop', {static: false}) imageDrop!: ElementRef;

  protected readonly facade = inject(StoreBrandingBannerFacade);

  ngOnInit(): void {
    this.facade.init(this.store);
  }
}
