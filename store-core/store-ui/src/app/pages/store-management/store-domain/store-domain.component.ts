import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../services/store.service';
import {NbToastrService} from "@nebular/theme";
import {TranslateService} from '@ngx-translate/core';
import {Page} from "../../shared/models/Page";
import {ColumnMode} from "@swimlane/ngx-datatable";
import {validators} from "../../shared/validation/validators";

@Component({
  selector: 'ngx-store-domain',
  templateUrl: './store-domain.component.html',
  styleUrls: ['./store-domain.component.scss']
})
export class StoreDomainComponent implements OnInit {
  isSubmited=false
  code;
  loading = false;
  page: Page = new Page();
  rows = [];
  perPage = 10;
  currentPage = 2;
  protected readonly ColumnMode = ColumnMode;
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
  ) {
  }

  ngOnInit() {
    this.code = this.activatedRoute.snapshot.paramMap.get('code');
    this.createForm();
    this.getAllocations();
  }

  route(link) {
    this.router.navigate(['pages/store-management/' + link + "/", this.code]);
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getAllocations();
  }

  getAllocations() {
    this.loading = true;
    this.storeService.getAllocations(this.code).subscribe({
      next: (it) => {
        this.loading = false;
        if (it && it.length > 0) {
          this.rows = it
          this.page.totalPages = 1
          this.page.totalElements = it.length
          this.page.size = it.length
        }
      },
      error: (err) => {
        this.loading = false;
      }
    });
  }

  onDelete(domain: string) {
    this.loading = true;
    this.storeService.removeDomain(this.code, domain).subscribe({
        next: (it) => {
          this.loading = false;
          this.toastr.success(this.translate.instant('STORE.DOMAIN_REMOVED'));
          this.getAllocations();
        },
        error: (err) => {
          this.loading = false;
          this.toastr.success(this.translate.instant('STORE.ERROR_REMOVING_DOMAIN'));
        }
      }
    )
  }

  createDomain(domain: string) {
    this.loading = true;
    this.storeService.allocateDomain(this.code, domain).subscribe({
        next: (it) => {
          this.loading = false;
          this.toastr.success(this.translate.instant('STORE.DOMAIN_CREATED'));
          this.getAllocations();
        },
        error: (err) => {
          this.loading = false;
          this.toastr.success(this.translate.instant('STORE.ERROR_CREATING_DOMAIN'));
        },
        complete: () => {
          this.form.reset();
        }
      }
    )
  }

  private createForm() {
    this.form = this.fb.group({
      domain: ['', [Validators.required, Validators.pattern(validators.domainPattern)]],
    });
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


  get domain() {
    return this.form.get('domain');
  }

}
