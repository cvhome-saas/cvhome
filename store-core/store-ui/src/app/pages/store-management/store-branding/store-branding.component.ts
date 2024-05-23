import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../services/store.service';
import {NbToastrService} from "@nebular/theme";
import {TranslateService} from '@ngx-translate/core';

@Component({
  selector: 'ngx-store-branding',
  templateUrl: './store-branding.component.html',
  styleUrls: ['./store-branding.component.scss']
})
export class StoreBrandingComponent implements OnInit {
  store;
  loading = false;
  loadingButton = false;
  selectedItem = '0';
  sidemenuLinks = [
    {
      id: '0',
      title: 'Store branding',
      key: 'COMPONENTS.STORE_BRANDING',
      link: 'store-branding'
    },
    {
      id: '1',
      title: 'Store home page',
      key: 'COMPONENTS.STORE_LANDING',
      link: 'store-landing'
    },
    {
      id: '2',
      title: 'Store domain',
      key: 'COMPONENTS.STORE_DOMAIN',
      link: 'store-domain'
    },
    {
      id: '3',
      title: 'Store details',
      key: 'COMPONENTS.STORE_DETAILS',
      link: 'store'
    }
  ];


  form: FormGroup;


  constructor(
    private storeService: StoreService,
    private formBuilder: FormBuilder,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
  ) {
    this.createForm();
  }

  ngOnInit() {
    this.loading = true;
    const code = this.activatedRoute.snapshot.paramMap.get('code');
    this.storeService.getStore(code).subscribe(it=>{
      this.store=it;
      this.loading = false;
    })


  }

  route(link) {
    this.router.navigate(['pages/store-management/' + link + "/", this.store.code]);
  }

  private createForm() {
    this.form = this.formBuilder.group({
      socialNetworks: this.formBuilder.array([])
    });

  }

  fillForm(socialNetworksArray) {
    const control = <FormArray>this.form.controls.socialNetworks;
    socialNetworksArray.forEach(el => {
      control.push(
        this.formBuilder.group({
          id: el.id,
          active: el.active,
          key: el.key,
          type: el.type,
          value: el.value
        })
      );
    });
  }

  get socialNetworks(): FormArray {
    return <FormArray>this.form.get('socialNetworks');
  }

  saveNetworks() {
    this.storeService.updateSocialNetworks(this.form.value)
      .subscribe(res => {
        this.toastr.success(this.translate.instant('STORE_BRANDING.NETWORKS_UPDATED'));
      });
  }
}
