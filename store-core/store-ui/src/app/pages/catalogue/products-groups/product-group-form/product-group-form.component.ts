import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';

import {ProductGroupsService} from '../services/product-groups.service';
import {ActivatedRoute, Router} from '@angular/router';

import {StorageService} from '../../../shared/services/storage.service';
import {validators} from '../../../shared/validation/validators';
import {TranslateService} from '@ngx-translate/core';

import {ProductService} from '../../products/services/product.service';

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
  selectedItems = [];
  dropdownSettings = {};
  perPage = 50;
  params = this.loadParams();
  itemsParams = this.loadItemsParams();
  products: Array<any> = [];
  store: string;
  action: string;

  constructor(
    private fb: FormBuilder,
    private productGroupsService: ProductGroupsService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private storageService: StorageService,
    private productService: ProductService,
    private translate: TranslateService) {
  }

  loadParams() {
    return {
      store: "",
      lang: this.storageService.getLanguage(),
      count: this.perPage,
      page: 0
    };
  }

  loadItemsParams() {
    return {
      store: "",
      lang: this.storageService.getLanguage()
    };
  }

  ngOnInit() {
    this.activatedRoute.params.subscribe(it => {
      const split: string[] = it['code'].split("-");
      this.params.store = split[0];
      this.itemsParams.store = split[0];
      if (split.length == 2 && split[1] != "") {
        this.action = 'edit';
        this.uniqueCode = split[1];
        this.getProductByCode();
      }
      this.getProductList();
    });
    this.createForm();
  }

  getProductList() {
    this.productService.getListOfProducts(this.params)
      .subscribe(res => {
        let temp = []
        res.products.map((value) => {
          temp.push({'id': value.id, 'name': value.description.name})
        });
        this.products = temp;
      });
  }

  getProductByCode() {
    this.productGroupsService.getProductsByGroup( this.uniqueCode, this.itemsParams)
      .subscribe(res => {
        let temp = []
        res.products.map((value) => {
          temp.push({'id': value.id, 'name': value.description.name})
        });
        this.selectedItems = temp;

        this.createForm();

        this.fillForm(res.productGroup);
      });
  }

  onFilterChange(e) {
    if (e.length > 3) {
      this.params["name"] = e;
      this.getProductList();
    }
    if (e === '') {
      this.params = this.loadParams();
      this.getProductList();
    }
  }

  get code() {
    return this.form.get('code');
  }

  private createForm() {
    this.form = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(validators.alphanumericwithhyphen)]],
      active: [true],
      product: [this.selectedItems]
    });
  }

  private fillForm(data) {
    this.form.patchValue({
      code: data.code,
      active: data.active,
    });

  }

  checkCode(event) {
    // const code = event.target.value.trim();
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

  onItemSelect(item: any) {
    this.addProductToGroup(item.id, this.uniqueCode)
    // this.loading = true;
  }

  onItemDeSelect(item: any) {
    this.removeProductFromGroup(item.id, this.uniqueCode)
  }

  addProductToGroup(productId, groupCode) {
    this.loading = true;
    this.productGroupsService.addProductToGroup(this.params.store, productId, groupCode)
      .subscribe(res => {
        this.loading = false;
      });
  }

  removeProductFromGroup(productId, groupCode) {
    this.loading = true;
    this.productGroupsService.removeProductFromGroup(this.params.store, productId, groupCode)
      .subscribe(res => {
        this.loading = false;
      });
  }

}
