import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';

import {ProductGroupsService} from '../services/product-groups.service';
import {ActivatedRoute, Router} from '@angular/router';
import {validators} from '../../../shared/validation/validators';
import {TranslateService} from '@ngx-translate/core';
import {ColumnMode} from "@swimlane/ngx-datatable";
import {Page} from "../../../shared/models/Page";
import {NbToastrService} from "@nebular/theme";
import {ErrorService} from "../../../shared/services/error.service";
import {SelectedStoreService} from "../../../shared/services/selected-store.service";
import {ConfigService} from "../../../shared/services/config.service";
import {switchMap, zip} from "rxjs";

@Component({
  selector: 'ngx-product-group-form',
  standalone: false,
  templateUrl: './product-group-form.component.html',
  styleUrls: ['./product-group-form.component.scss']
})
export class ProductGroupFormComponent implements OnInit {
  form: FormGroup;
  loader: boolean = false;
  perPageSize = 50;
  isCodeUnique = true;
  store: string;
  uniqueCode: string;
  action: string;
  languages: any[] = [];
  defaultLanguage: string;
  rows: Array<any> = [];
  page: Page = new Page();
  protected readonly ColumnMode = ColumnMode;
  private isLoaded: boolean = false;

  constructor(
    private fb: FormBuilder,
    private productGroupsService: ProductGroupsService,
    private router: Router,
    private activatedRoute: ActivatedRoute,
    private errorService: ErrorService,
    private toastr: NbToastrService,
    private translate: TranslateService,
    private selectedStoreService: SelectedStoreService,
    private configService: ConfigService,
  ) {
  }

  get code() {
    return this.form.get('code');
  }

  get selectedLanguage() {
    return this.form.get('selectedLanguage');
  }

  get descriptions(): FormArray {
    return this.form.get('descriptions') as FormArray;
  }

  ngOnInit() {
    this.createForm();

    zip([
      this.selectedStoreService.current(),
      this.activatedRoute.params,
      this.activatedRoute.queryParams,
    ]).pipe(
      switchMap(([store, params, queryParams]) => {
        this.store = store as string;
        this.uniqueCode = params['code'];
        if (!this.uniqueCode) {
          const suggestedCode = queryParams['code'];
          if (suggestedCode) {
            this.form.patchValue({code: suggestedCode});
          }
        } else {
          this.action = 'edit';
        }
        return this.configService.getListOfSupportedLanguages(this.store);
      })
    ).subscribe({
      next: (languages) => {
        this.languages = [...languages];
        this.defaultLanguage = this.languages[0]?.code;
        this.addFormArray();
        this.form.patchValue({selectedLanguage: this.defaultLanguage});
        if (this.action === 'edit') {
          this.loadGroup();
        }
      },
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err),
    });
  }

  addFormArray() {
    const control = this.descriptions;
    this.languages.forEach(lang => {
      control.push(this.fb.group({
        language: [lang.code, [Validators.required]],
        name: ['', [Validators.required]],
      }));
    });
  }

  loadGroup() {
    this.loader = true;
    this.productGroupsService.getProductGroup(this.uniqueCode)
      .subscribe({
        next: (group) => {
          this.rows = group.products || [];
          this.page.totalPages = 1;
          this.page.totalElements = this.rows.length;
          this.page.size = this.rows.length;
          this.fillForm(group);
          this.loader = false;
          this.isLoaded = true;
        },
        error: (err) => {
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
      });
  }

  fillForm(data: any) {
    this.form.patchValue({
      code: data.code,
      active: data.active,
      selectedLanguage: this.defaultLanguage,
    });
    this.fillFormArray(data.descriptions || []);
  }

  fillFormArray(descriptions: any[]) {
    this.descriptions.controls.forEach((ctrl, index) => {
      const desc = descriptions.find((d: any) => d.language === ctrl.get('language').value);
      if (desc) {
        ctrl.patchValue({name: desc.name});
      }
    });
  }

  selectLanguage(lang: string) {
    this.form.patchValue({selectedLanguage: lang});
  }

  checkCode(event: any) {
    const code = event.target.value.trim();
    if (!code || this.action === 'edit') return;
    this.productGroupsService.checkCode(code).subscribe({
      next: (res) => this.isCodeUnique = !res.exists,
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err),
    });
  }

  descriptionNameStatus(ctrl: any): string {
    const name = ctrl.get('name');
    if (!name.dirty && !name.touched) return 'basic';
    return name.invalid ? 'danger' : 'success';
  }

  languageTabStatus(langCode: string): string {
    const ctrl = this.descriptions.controls.find(c => c.get('language').value === langCode);
    if (ctrl && ctrl.get('name').invalid && ctrl.get('name').touched) return 'danger';
    return this.selectedLanguage.value === langCode ? 'primary' : 'basic';
  }

  save() {
    this.form.markAllAsTouched();
    if (this.form.invalid || !this.isCodeUnique) {
      return;
    }
    this.loader = true;
    this.productGroupsService.createProductGroup(this.prepareSaveData()).subscribe({
      next: () => {
        this.loader = false;
        this.router.navigate(['pages/catalogue/products-groups/groups-list']);
      },
      error: (err) => {
        this.loader = false;
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      },
    });
  }

  goToBack() {
    this.router.navigate(['pages/catalogue/products-groups/groups-list']);
  }

  onItemSelect(item: any, groupCode: string) {
    this.loader = true;
    this.productGroupsService.addProductToGroup(item.id, groupCode)
      .subscribe({
        next: () => {
          this.loader = false;
          this.rows = [...this.rows, item];
          this.page.totalPages = 1;
          this.page.totalElements = this.rows.length;
          this.page.size = this.rows.length;
          this.toastr.success(this.translate.instant('PRODUCT_GROUP.PRODUCT_ADDED'));
        },
        error: (err) => {
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
      });
  }

  onItemDeSelect(item: any, groupCode: string) {
    this.loader = true;
    this.productGroupsService.removeProductFromGroup(item.id, groupCode)
      .subscribe({
        next: () => {
          this.loader = false;
          this.rows = this.rows.filter(it => it.id !== item.id);
          this.page.totalPages = 1;
          this.page.totalElements = this.rows.length;
          this.page.size = this.rows.length;
          this.toastr.success(this.translate.instant('PRODUCT_GROUP.PRODUCT_REMOVED'));
        },
        error: (err) => {
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        },
      });
  }

  setPage(pageInfo: any) {
    if (!this.uniqueCode || !this.isLoaded) return;
    this.page.pageNumber = pageInfo.offset;
    this.loadGroup();
  }

  private createForm() {
    this.form = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(validators.alphanumericwithhyphen)]],
      active: [true],
      selectedLanguage: ['', [Validators.required]],
      descriptions: this.fb.array([]),
    });
  }

  private prepareSaveData() {
    const {code, active, descriptions} = this.form.value;
    return {code, active, descriptions};
  }
}
