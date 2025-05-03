import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';

import {ProductGroupsService} from '../services/product-groups.service';
import {ActivatedRoute, Router} from '@angular/router';
import {validators} from '../../../../shared/validation/validators';
import {TranslateService} from '@ngx-translate/core';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {Page} from "../../../../shared/models/Page";
import {NbToastrService} from "@nebular/theme";
import {ErrorService} from "../../../../shared/services/error.service";

@Component({
  selector: 'ngx-product-group-form',
  standalone:false,
  templateUrl: './product-group-form.component.html',
  styleUrls: ['./product-group-form.component.scss']
})
export class ProductGroupFormComponent implements OnInit {
  form: FormGroup;
  isCodeUnique = true;
  uniqueCode: string;
  loader: boolean = false;
  perPage = 50;
  params :any;
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
    private errorService: ErrorService,
    private toastr: NbToastrService,
    private translate: TranslateService) {
    this.params=this.loadParams();
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
      .subscribe(it => {
        this.rows = it.content;
        this.page.totalPages = 1
        this.page.totalElements = this.rows.length
        this.page.size = this.rows.length
        this.createForm();
        this.fillForm(it.productGroup);
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
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
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  goToBack() {
    this.router.navigate(['pages/catalogue/products-groups/groups-list']);
  }

  onItemSelect(item: any, groupCode: string) {
    this.loader = true;
    this.productGroupsService.addProductToGroup(this.params.store, item.id, groupCode)
      .subscribe({
        next: (it) => {
          this.loader = false;
          this.rows.push(item)
          this.rows = this.rows.map(it => it);
          this.page.totalPages = 1
          this.page.totalElements = this.rows.length
          this.page.size = this.rows.length
          this.toastr.success(this.translate.instant('PRODUCT_GROUP.PRODUCT_ADDED'))
        },
        error: (err) => {
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
      });
  }

  onItemDeSelect(item, groupCode) {
    this.loader = true;
    this.productGroupsService.removeProductFromGroup(this.params.store, item.id, groupCode)
      .subscribe({
        next: (data) => {
          this.loader = false;
          this.rows = this.rows.filter(it => it.id != item.id)
          this.page.totalPages = 1
          this.page.totalElements = this.rows.length
          this.page.size = this.rows.length
          this.toastr.success(this.translate.instant('PRODUCT_GROUP.PRODUCT_REMOVED'))
        },
        error: (err) => {
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
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
