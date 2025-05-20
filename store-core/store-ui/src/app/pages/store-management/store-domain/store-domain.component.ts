import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../services/store.service';
import {NbToastrService} from "@nebular/theme";
import {TranslateService} from '@ngx-translate/core';
import {Page} from "../../../shared/models/Page";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {validators} from "../../../shared/validation/validators";
import {ErrorService} from "../../../shared/services/error.service";
import {zip} from "rxjs";
import {DnsCheckService} from "../services/dns-check.service";
import {dnsPointsToPodValidatorFactory, StoreDomainComponentValidatorContext} from "./store-domain.validator";

@Component({
  selector: 'ngx-store-domain',
  standalone: false,
  templateUrl: './store-domain.component.html',
  styleUrls: ['./store-domain.component.scss']
})
export class StoreDomainComponent implements OnInit , StoreDomainComponentValidatorContext{
  isSubmited = false
  store;
  loading = false;
  page: Page = new Page();
  rows = [];
  selectedItem = '2';
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
  protected readonly ColumnMode = ColumnMode;
  saasProperties = {alis: '', domain: ''};
  podId = {id: ''};

  constructor(
    private storeService: StoreService,
    private fb: FormBuilder,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private errorService: ErrorService,
    private dnsCheckService: DnsCheckService
  ) {
  }

  get domain() {
    return this.form.get('domain');
  }

  ngOnInit() {
    this.store = this.activatedRoute.snapshot.paramMap.get('code');
    this.createForm();
    this.getAllocations();
  }

  route(link) {
    this.router.navigate(['pages/store-management/' + link + "/", this.store]);
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getAllocations();
  }

  getAllocations() {

    this.loading = true;
    zip(this.storeService.saasProperties(), this.storeService.getAllocations(this.store), this.storeService.storePodByStoreId(this.store))
      .subscribe({
        next: (it) => {
          this.loading = false;
          this.saasProperties = it[0];
          this.podId = it[2].id;
          if (it && it[1].data && it[1].data.length > 0) {
            this.rows = it[1].data
            this.page.totalPages = 1
            this.page.totalElements = it[1].data.length
            this.page.size = it[1].data.length
          }
        },
        error: (err) => {
          this.loading = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
        complete: () => {
          this.loading = false;
        }
      })
  }

  onAccess(row: any) {
    window.open(`https://${this.generateDomain(row)}`, '_blank');
  }

  onDelete(domain: string) {
    this.loading = true;
    this.storeService.removeDomain(this.store, domain).subscribe({
        next: (it) => {
          this.loading = false;
          this.toastr.success(this.translate.instant('STORE.DOMAIN_REMOVED'));
          this.getAllocations();
        },
        error: (err) => {
          this.loading = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      }
    )
  }

  createDomain(domain: string) {
    this.loading = true;
    this.storeService.allocateDomain(this.store, domain).subscribe({
        next: (it) => {
          this.loading = false;
          this.toastr.success(this.translate.instant('STORE.DOMAIN_CREATED'));
          this.getAllocations();
        },
        error: (err) => {
          this.loading = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
        complete: () => {
          this.form.reset();
          this.isSubmited = false;
        }
      }
    )
  }

  save() {
    this.isSubmited = true;
    this.form.markAllAsTouched();
    if (!this.form.valid) {
      return;
    }
    const object = this.form.value;
    this.createDomain(object.domain)
  }

  private createForm() {
    this.form = this.fb.group({
      domain: [
        '',
        {
          validators: [Validators.required, Validators.pattern(validators.domainPattern)],
          asyncValidators: [dnsPointsToPodValidatorFactory(this, this.dnsCheckService)],
          updateOn: 'blur'
        }
      ],
    });
  }

  public generateDomain(row: any): string {
    if (row.domainType === "SUB_DOMAIN") {
      return row.domain + "." + this.podServerDomain();
    } else {
      return row.domain;
    }
  }

  public podServerDomain() {
    return this.saasProperties.alis + "-" + this.podId.id + "." + this.saasProperties.domain;
  }
}
