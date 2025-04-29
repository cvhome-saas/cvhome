import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfigService} from '../../../shared/services/config.service';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {validators} from '../../../shared/validation/validators';
import {slugify} from '../../../shared/utils/slugifying';

import {TranslateService} from '@ngx-translate/core';
import {ImageBrowserComponent} from "../../../shared/components/image-browser/image-browser.component";
import {ErrorService} from "../../../shared/services/error.service";
import {ContentService} from "../services/content.service";

declare var $: any;

@Component({
  selector: 'add-page',
  standalone:false,
  templateUrl: './add-page.component.html',
  styleUrls: ['./add-page.component.scss'],
})
export class AddPageComponent implements OnInit {

  loader = false;
  store: string;
  uniqueCode: string;
  form: FormGroup;
  content: any;
  loadingList = false;
  action: string = 'save';
  languages = [];

  defaultLanguage: string;
  currentLanguage: string;

  isCodeExists = false;

  constructor(
    private contentService: ContentService,
    public router: Router,
    private toastr: NbToastrService,
    private configService: ConfigService,
    private dialogService: NbDialogService,
    private activatedRoute: ActivatedRoute,
    private fb: FormBuilder,
    private translate: TranslateService,
    private errorService: ErrorService
  ) {
    this.defaultLanguage = this.translate.defaultLang;
    this.currentLanguage = this.translate.currentLang;
  }

  get code() {
    return this.form.get('code');
  }

  get descriptions(): FormArray {
    return <FormArray>this.form.get('descriptions');
  }

  get selectedLanguage() {
    return this.form.get('selectedLanguage');
  }

  ngOnInit() {
    this.loader = true;
    this.activatedRoute.params
      .subscribe(it => {
        const split: string[] = it['code'].split("-");
        this.store = split[0];
        this.getLanguages();
        if (split.length == 2 && split[1] != "") {
          this.action = 'edit';
          this.uniqueCode = split[1];
          this.getPage();
        }
      });

    this.createForm();
  }

  addFormArray() {
    const control = <FormArray>this.form.controls.descriptions;
    this.languages.forEach(lang => {
      control.push(
        this.fb.group({
          language: [lang.code, [Validators.required]],
          description: [''],
          title: [''],
          metaDescription: [''],
          name: ['', [Validators.required]],
          friendlyUrl: ['', [Validators.required]]
        })
      );
    });
  }

  fillFormArray() {
    this.form.value.descriptions.forEach((desc, index) => {
      if (this.content != null && this.content.descriptions) {
        this.content.descriptions.forEach((description) => {
          if (desc.language === description.language) {
            (<FormArray>this.form.get('descriptions')).at(index).patchValue({
              language: description.language,
              name: description.name,
              friendlyUrl: description.friendlyUrl,
              description: description.description,
              title: description.title,
              metaDescription: description.metaDescription,
            });
          }
        });
      }
    });
  }

  public findInvalidControls() {
    const invalid = [];
    console.log(this.form.controls)
    const controls = this.form.controls;
    for (const name in controls) {
      if (controls[name].invalid) {
        invalid.push(name);
      }
    }
    if (invalid.length > 0) {
      this.toastr.danger(this.translate.instant('COMMON.FILL_REQUIRED_FIELDS'));
    }
    return invalid;
  }

  selectLanguage(lang) {
    this.form.patchValue({
      selectedLanguage: lang,
    });
    this.currentLanguage = lang;
    this.fillFormArray();
  }

  focusOutFunction(event) {
    const code = event.target.value.trim();
    this.contentService.checkCodePageExist(code, this.store)
      .subscribe(res => {
        this.isCodeExists = res.exists;
      }, err => {
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      });
  }

  urlTitle(event, index) {
    (<FormArray>this.form.get('descriptions')).at(index).patchValue({
      friendlyUrl: slugify(event)
    });
  }

  save() {
    this.form.markAllAsTouched();
    if (this.findInvalidControls().length > 0) {
      return;
    }
    const object = this.form.value;

    //remove un necessary
    delete object.selectedLanguage;

    if (object.id) {

      this.contentService.updatePage(object.id, this.store, object)
        .subscribe({
          next: (data) => {
            this.loadingList = false;
            this.toastr.success('Page updated successfully');
            this.router.navigate(['/pages/content/pages/list']);
          },
          error: (err) => {
            this.loadingList = false;
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          },
        });
    } else {
      this.contentService.createPage(this.store, object)
        .subscribe({
          next: (data) => {
            this.loadingList = false;
            this.toastr.success('Page added successfully');
            this.router.navigate(['/pages/content/pages/list']);
          },
          error: (err) => {
            this.loadingList = false;
            this.errorService.error('ERROR.SYSTEM_ERROR', err);
          },
        });
    }

  }

  goToBack() {
    this.router.navigate(['/pages/content/pages/list']);
  }

  private getLanguages() {
    console.log("will get lang")
    this.configService.getListOfSupportedLanguages(this.store)
      .subscribe({
        next: (languages) => {
          this.languages = [...languages];
          this.addFormArray();
          this.loader = false;
        },
        error: (err) => {
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
          this.loader = false;
        },
      });
  }

  private getPage() {
    this.contentService.getPage(this.uniqueCode, this.store)
      .subscribe({
        next: (data) => {
          this.content = data;
          this.fillForm();
          this.loader = false;
        },
        error: (err) => {
          this.loader = false;
          this.errorService.error('ERROR.SYSTEM_ERROR', err);
        }
      });
  }

  private createForm() {
    this.form = this.fb.group({
      id: [''],
      code: ['', [Validators.required, Validators.pattern(validators.alphanumeric)]],
      visible: [false],
      linkToMenu: [false],
      order: [0],
      selectedLanguage: [this.defaultLanguage, [Validators.required]],
      descriptions: this.fb.array([]),
    });
  }

  private fillForm() {
    this.form.patchValue({
      id: this.content.id,
      code: this.content.code,
      visible: this.content.visible,
      linkToMenu: this.content.linkToMenu,
      selectedLanguage: this.defaultLanguage,
      descriptions: [],
    });
    this.fillFormArray();
    this.findInvalidControls();

  }
}
