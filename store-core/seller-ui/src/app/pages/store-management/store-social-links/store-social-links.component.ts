import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../services/store.service';
import {NbToastrService} from "@nebular/theme";
import {TranslateService} from '@ngx-translate/core';
import {ErrorService} from "../../shared/services/error.service";
import {zip} from "rxjs";

@Component({
  selector: 'ngx-store-social-links',
  standalone: false,
  templateUrl: './store-social-links.component.html',
  styleUrls: ['./store-social-links.component.scss']
})
export class StoreSocialLinksComponent implements OnInit {
  isSubmited = false
  store;
  providers: string[] = [];
  loading = false;
  rows = [];
  selectedItem = '3';
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
      title: 'Store Social Links',
      key: 'COMPONENTS.STORE_SOCIAL_LINKS',
      link: 'store-social-links'
    },
    {
      id: '4',
      title: 'Store Slider Images',
      key: 'COMPONENTS.STORE_SLIDER_IMAGES',
      link: 'store-slider-images'
    },
    {
      id: '5',
      title: 'Store details',
      key: 'COMPONENTS.STORE_DETAILS',
      link: 'store'
    }
  ];
  form: FormGroup;

  constructor(
    private storeService: StoreService,
    private fb: FormBuilder,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private errorService: ErrorService
  ) {
  }

  get domain() {
    return this.form.get('domain');
  }

  ngOnInit() {
    zip(this.storeService.getStore(this.activatedRoute.snapshot.paramMap.get('code')), this.storeService.getSupportedSocialLinkProviders())
      .subscribe({
        next: ([store, providers]) => {
          this.store = store;
          this.providers = providers;
          this.createForm();

        },
        error: (err) => {
          this.loading = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      })
  }

  route(link) {
    this.router.navigate(['pages/store-management/' + link + "/", this.store.id]);
  }


  save() {
    this.isSubmited = true;
    this.form.markAllAsTouched();
    if (!this.form.valid) {
      return;
    }
    const socialLinks = [];
    Object.keys(this.form.value).forEach((it, i) => {
      socialLinks.push({provider: it, url: this.form.value[it]})
    })
    // this.store.socialLinks = data;
    this.storeService.updateStoreSocialLinks(this.store.id, {
      socialLinks
    })
      .subscribe(store => {
        this.toastr.success(this.translate.instant('STORE.SOCIAL_LINKS_UPDATED'));
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });


  }

  private createForm() {
    const links = {};
    let controls = {};
    this.store.socialLinks.forEach(it => {
      links[it.provider] = it.url;
    })

    this.providers.forEach(it => {
      controls[it] = [links[it], []]
    })

    this.form = this.fb.group(controls);
  }

}
