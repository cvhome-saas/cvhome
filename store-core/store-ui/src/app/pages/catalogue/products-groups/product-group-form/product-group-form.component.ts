import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';

import {ProductGroupsService} from '../services/product-groups.service';
import {ActivatedRoute, Router} from '@angular/router';

import {StorageService} from '../../../shared/services/storage.service';
import {validators} from '../../../shared/validation/validators';
import {TranslateService} from '@ngx-translate/core';

import {ProductService} from '../../products/services/product.service';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {Page} from "../../../shared/models/Page";
import {NbToastrService} from "@nebular/theme";

@Component({
  selector: 'ngx-product-group-form',
  templateUrl: './product-group-form.component.html',
  styleUrls: ['./product-group-form.component.scss']
})
export class ProductGroupFormComponent implements OnInit {
  form: FormGroup;
  isCodeUnique = true;
  uniqueCode: string;
  loading: boolean = false;
  perPage = 50;
  params = this.loadParams();
  products: Array<any> = [];
  rows: Array<any> = [];
  store: string;
  action: string;
  page: Page = new Page();
  protected readonly ColumnMode = ColumnMode;

  constructor(
    private fb: FormBuilder,
    private productGroupsService: ProductGroupsService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private storageService: StorageService,
    private productService: ProductService,
    private toastr: NbToastrService,
    private translate: TranslateService) {
  }

  get code() {
    return this.form.get('code');
  }

  loadParams() {
    return {
      store: "",
      lang: this.translate.currentLang,
      count: this.perPage,
      page: 0
    };
  }

  ngOnInit() {
    this.activatedRoute.params.subscribe(it => {
      const split: string[] = it['code'].split("-");
      this.params.store = split[0];
      if (split.length == 2 && split[1] != "") {
        this.action = 'edit';
        this.uniqueCode = split[1];
        this.getProductByCode();
      }
    });
    this.createForm();
  }

  getProductByCode() {
    this.productGroupsService.getProductsByGroup(this.uniqueCode, this.params)
      .subscribe(res => {
        this.rows = res.products;
        this.page.totalPages = 1
        this.page.totalElements = this.rows.length
        this.page.size = this.rows.length
        this.createForm();
        this.fillForm(res.productGroup);
      });
  }

  checkCode(event) {
    const code = event.target.value.trim();
    this.uniqueCode = code;
    // this.productGroupsService.checkGroupCode(code)
    //   .subscribe(res => {
    //     this.isCodeUnique = !(res.exists && (this.option.code !== code));
    //   });
  }

  save() {
    this.productGroupsService.createProductGroup(this.params.store, this.form.value).subscribe(res => {
      this.router.navigate(['pages/catalogue/products-groups/groups-list']);
    });
  }

  update() {
    this.productGroupsService.updateGroupActiveValue(this.params.store, this.form.value)
      .subscribe(res => {
        this.router.navigate(['pages/catalogue/products-groups/groups-list']);
      });
  }

  goToBack() {
    this.router.navigate(['pages/catalogue/products-groups/groups-list']);
  }

  onItemSelect(item: any, groupCode: string) {
    this.loading = true;
    this.productGroupsService.addProductToGroup(this.params.store, item.id, groupCode)
      .subscribe({
        next: (data) => {
          this.loading = false;
          this.rows.push(item)
          this.rows = this.rows.map(it => it);
          this.page.totalPages = 1
          this.page.totalElements = this.rows.length
          this.page.size = this.rows.length
          this.toastr.success(this.translate.instant('PRODUCT_GROUP.PRODUCT_ADDED'))
        },
        error: (err) => {
          this.loading = false;
          this.toastr.danger(this.translate.instant('PRODUCT_GROUP.PRODUCT_ADDED_ERROR'))
        },
      });
  }

  onItemDeSelect(item, groupCode) {
    this.loading = true;
    this.productGroupsService.removeProductFromGroup(this.params.store, item.id, groupCode)
      .subscribe({
        next: (data) => {
          this.loading = false;
          this.rows = this.rows.filter(it => it.id != item.id)
          this.page.totalPages = 1
          this.page.totalElements = this.rows.length
          this.page.size = this.rows.length
          this.toastr.success(this.translate.instant('PRODUCT_GROUP.PRODUCT_REMOVED'))
        },
        error: (err) => {
          this.loading = false;
          this.toastr.danger(this.translate.instant('PRODUCT_GROUP.PRODUCT_REMOVED_ERROR'))
        },
      });
  }

  setPage(pageInfo) {
    this.page.pageNumber = pageInfo.offset;
    this.getProductByCode();
  }

  private createForm() {
    this.form = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(validators.alphanumericwithhyphen)]],
      active: [true],
      product: [this.rows]
    });
  }

  private fillForm(data) {
    this.form.patchValue({
      code: data.code,
      active: data.active,
    });

  }
}
