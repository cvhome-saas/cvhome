import {Component, Input, OnInit, ViewChild, ElementRef, inject} from "@angular/core";
import {StoreBrandingLogoFacade} from "./facades/store-branding-logo.facade";

@Component({
  selector: 'ngx-store-branding-logo',
  standalone: false,
  template: `
    <form [formGroup]="facade.imageUpload">
      <div (click)='imageInput.click()' (dragover)="facade.allowDrop($event)" (drop)="facade.drop($event, imageDrop)" class="wrapper">
        <div #imageDrop id="imageDrop">
          <img *ngIf="facade.logo()" [src]="facade.logo()?.path" style="height:250px"/>
        </div>
        <div class="uploadingPhrase">{{ 'STORE_BRANDING.DROP_FILE' | translate }}</div>
      </div>

      <input nbInput fullWidth #imageInput (change)='facade.imageChange($event, imageDrop)' formControlName="imageInput" id="imageInput" required
             type="file">

      <div class="form-group actions-button">
        <button nbButton status="danger" (click)="facade.removeLogo(store.id, imageDrop)" *ngIf="facade.showRemoveButton()" type="submit">{{
            'COMMON.REMOVE' | translate
          }}
        </button>
        <button nbButton status="success" (click)="facade.saveLogo(store.id)" *ngIf="facade.logoFile" [nbSpinner]="facade.loadingButton()"
                nbSpinnerMessage="" nbSpinnerSize="large" type="submit">
          {{ (!facade.loadingButton() ? 'COMMON.SAVE' : '') | translate }}
        </button>
      </div>
    </form>
  `,
  styleUrls: ['./store-branding.component.scss'],
  providers: [StoreBrandingLogoFacade]
})
export class StoreBrandingLogoComponent implements OnInit {
  @Input() store: any;
  @ViewChild('imageDrop', {static: false}) imageDrop!: ElementRef;

  protected readonly facade = inject(StoreBrandingLogoFacade);

  ngOnInit(): void {
    this.facade.init(this.store);
  }
}
