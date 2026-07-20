import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../services/store.service';
import {NbToastrService} from "@nebular/theme";
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from "../../shared/services/error.service";
import {sideMenuLinks} from "../services/constents";

@Component({
  selector: 'ngx-store-branding',
  standalone: false,
  templateUrl: './store-branding.component.html',
  styleUrls: ['./store-branding.component.scss']
})
export class StoreBrandingComponent implements OnInit {
  store;
  loading = false;
  loadingButton = false;
  selectedItem = '0';
  protected readonly sideMenuLinks = sideMenuLinks;

  form: FormGroup;


  constructor(
    private storeService: StoreService,
    private formBuilder: FormBuilder,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private errorService: ErrorService,
  ) {
    this.createForm();
  }

  get socialNetworks(): FormArray {
    return <FormArray>this.form.get('socialNetworks');
  }

  ngOnInit() {
    this.loading = true;
    const store = this.activatedRoute.snapshot.paramMap.get('code');
    this.storeService.getStore(store).subscribe(it => {
      this.store = it;
      this.loading = false;
    }, err => {
      this.loading = false;
      this.errorService.error('ERROR.SYSTEM_ERROR', err);
    })


  }

  route(link) {
    this.router.navigate(['pages/store-management/' + link + "/", this.store.id]);
  }

  private createForm() {
    this.form = this.formBuilder.group({
      socialNetworks: this.formBuilder.array([])
    });

  }
}
