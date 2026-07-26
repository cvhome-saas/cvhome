import {Component, OnInit, inject} from '@angular/core';
import {AddPageFacade} from './facades/add-page.facade';
import {AddPageFormService} from './services/add-page.form.service';

@Component({
  selector: 'add-page',
  standalone: false,
  templateUrl: './add-page.component.html',
  styleUrls: ['./add-page.component.scss'],
  providers: [AddPageFacade, AddPageFormService]
})
export class AddPageComponent implements OnInit {
  protected readonly facade = inject(AddPageFacade);

  get form() { return this.facade.form; }
  get code() { return this.facade.code; }
  get descriptions() { return this.facade.descriptions; }
  get selectedLanguage() { return this.facade.selectedLanguage; }
  get loader() { return this.facade.loader(); }
  get action() { return this.facade.action(); }
  get languages() { return this.facade.languages(); }
  get defaultLanguage() { return this.facade.defaultLanguage(); }
  get currentLanguage() { return this.facade.currentLanguage(); }
  get uniqueCode() { return this.facade.uniqueCode(); }
  get isCodeExists() { return this.facade.isCodeExists(); }

  ngOnInit(): void {
    this.facade.init();
  }

  selectLanguage(lang: string): void {
    this.facade.selectLanguage(lang);
  }

  focusOutFunction(event: any): void {
    this.facade.focusOutFunction(event);
  }

  urlTitle(event: any, index: number): void {
    this.facade.urlTitle(event, index);
  }

  goToBack(): void {
    this.facade.goToBack();
  }

  save(): void {
    this.facade.save();
  }
}
