import {Component, OnInit} from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {CrudService} from '../../shared/services/crud.service';
import {ActivatedRoute, Router} from '@angular/router';
import {ConfigService} from '../../shared/services/config.service';
// import { ImageBrowserComponent } from '../../../@theme/components/image-browser/image-browser.component';
import {NbDialogService, NbToastrService} from '@nebular/theme';
import {validators} from '../../shared/validation/validators';
import {slugify} from '../../shared/utils/slugifying';

import {TranslateService} from '@ngx-translate/core';

declare var jquery: any;
declare var $: any;

@Component({
  selector: 'add-page',
  templateUrl: './add-page.component.html',
  styleUrls: ['./add-page.component.scss'],
})
export class AddPageComponent implements OnInit {

  loader = false;
  store: string;//identifier fromroute
  uniqueCode: string;//identifier fromroute
  form: FormGroup;
  content: any;

  loadingList = false;
  // visible: any = false;
  // descData: any;
  // updatedID: any;
  // linkToMenu: any = false;
  // code: string = '';
  // order: number = 0;
  action: string = 'save';
  // language: string = 'en';
  // description: Array<any> = []
  languages = [];

  defaultLanguage = localStorage.getItem('lang');
  //changed from seo section
  currentLanguage = localStorage.getItem('lang');

  isCodeExists = false;
  message: string = '';
  public scrollbarOptions = {axis: 'y', theme: 'minimal-dark'};


  editorConfig = {
    placeholder: '',
    tabsize: 2,
    height: 300,
    toolbar: [
      ['misc', ['codeview', 'fullscreen', 'undo', 'redo']],
      ['style', ['bold', 'italic', 'underline', 'clear']],
      ['font', ['bold', 'italic', 'underline', 'strikethrough', 'superscript', 'subscript', 'clear']],
      ['fontsize', ['fontname', 'fontsize', 'color']],
      ['para', ['style', 'ul', 'ol', 'height']],
      ['insert', ['table', 'link', 'video']],
      ['customButtons', ['testBtn']]
    ],

    buttons: {
      'testBtn': this.customButton.bind(this)
    },
    fontNames: ['Helvetica', 'Arial', 'Arial Black', 'Comic Sans MS', 'Courier New', 'Roboto', 'Times']
  };

  constructor(
    private crudService: CrudService,
    public router: Router,
    private toastr: NbToastrService,
    private configService: ConfigService,
    private dialogService: NbDialogService,
    private activatedRoute: ActivatedRoute,
    private fb: FormBuilder,
    private translate: TranslateService
  ) {

  }

  ngOnInit() {
    this.loader = true;
    this.activatedRoute.params.subscribe(it => {
      const split: string[] = it['code'].split("-");
      this.store = split[0];
      this.getLanguages();
      if (split.length == 2) {
        this.action = 'edit';
        this.uniqueCode = split[1];
        this.getPage();
      }
    });

    this.createForm();
  }

  private getLanguages() {
    this.configService.getListOfSupportedLanguages(this.store)
      .subscribe({
        next: (languages) => {
          this.languages = [...languages];
          this.addFormArray();//create array
        },
        error: (err) => {
          this.toastr.danger(err.error.message);
          this.loader = false;
        },
      });
  }

  private getPage() {
    this.crudService.get('/store/api/v1/content/pages/' + this.uniqueCode + '?lang=_all&store=' + this.store)
      .subscribe({
        next: (data) => {
          this.content = data;
          this.fillForm();
          this.loader = false;
        },
        error: (err) => {
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
      // console.log(name)
      // console.log(controls[name])
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

  get code() {
    return this.form.get('code');
  }

  get descriptions(): FormArray {
    return <FormArray>this.form.get('descriptions');
  }

  get selectedLanguage() {
    return this.form.get('selectedLanguage');
  }

  focusOutFunction(event) {
    console.log(event)
    const code = event.target.value.trim();
    this.crudService.get('/store/api/v1/private/content/page/' + code + '/exists')
      .subscribe(res => {
        this.isCodeExists = res.exists;
      });
  }

  urlTitle(event, index) {
    (<FormArray>this.form.get('descriptions')).at(index).patchValue({
      friendlyUrl: slugify(event)
    });
  }

  createPages() {
    this.form.markAllAsTouched();
    if (this.findInvalidControls().length > 0) {
      return;
    }

    // this.loadingList = true;

    //manouver resulting object
    var object = this.form.value;

    //remove un necessary
    delete object.selectedLanguage;


    console.log(object)


    if (object.id) {
      this.crudService.put('/store/api/v1/private/content/page/' + object.id+"?store="+this.store, object)
        .subscribe(data => {
          this.loadingList = false;
          this.toastr.success('Page updated successfully');
          this.router.navigate(['/pages/content/pages/list']);
        }, error => {
          this.loadingList = false;
        });
    } else {
      this.crudService.post('/store/api/v1/private/content/page', object)
        .subscribe(data => {
          console.log(data);
          this.loadingList = false;
          this.toastr.success('Page added successfully');
          this.router.navigate(['/pages/content/pages/list']);
        }, error => {
          this.loadingList = false;
        });
    }

  }


  goToback() {
    this.router.navigate(['/pages/content/pages/list']);
  }

  customButton(context) {
    const me = this;
    const ui = $.summernote.ui;
    const button = ui.button({
      contents: '<i class="note-icon-picture"></i>',
      tooltip: 'Gallery',
      container: '.note-editor',
      className: 'note-btn',
      click: function () {
        // me.dialogService.open(ImageBrowserComponent, {}).onClose.subscribe(name => name && context.invoke('editor.pasteHTML', '<img src="' + name + '">'));
      }
    });
    return button.render();
  }
}
