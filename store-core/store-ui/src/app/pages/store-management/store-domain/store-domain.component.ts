import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {StoreService} from '../services/store.service';
import {NbToastrService} from "@nebular/theme";
import {TranslateService} from '@ngx-translate/core';
import {Page} from "../../shared/models/Page";
import {ColumnMode} from "@swimlane/ngx-datatable";

@Component({
  selector: 'ngx-store-domain',
  templateUrl: './store-domain.component.html',
  styleUrls: ['./store-domain.component.scss']
})
export class StoreDomainComponent implements OnInit {
  code;
  loading = false;
  loadingButton = false;
  page: Page = new Page();
  rows = [];
  perPage = 10;
  currentPage = 2;
  protected readonly ColumnMode = ColumnMode;
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
  }

  ngOnInit() {
    this.loading = true;
    this.code = this.activatedRoute.snapshot.paramMap.get('code');
    this.getAllocations();
    // this.storeService.allocateDomain(this.code,'another.com').subscribe(it => {
    //   console.log(it);
    // })
  }

  route(link) {
    this.router.navigate(['pages/store-management/' + link + "/", this.code]);
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getAllocations();
  }

  getAllocations() {
    this.storeService.getAllocations(this.code).subscribe(it => {
      this.loading = false;
      if (it && it.length > 0) {
        this.rows = it
        this.page.totalPages = 1
        this.page.totalElements = it.length
        this.page.size = it.length
      }
    });
  }

  onDelete(domain) {
    this.storeService.removeDomain(this.code,domain).subscribe(it => {
      console.log(it);
    })
  }
}
