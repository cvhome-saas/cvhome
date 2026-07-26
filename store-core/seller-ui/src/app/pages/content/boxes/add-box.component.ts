import {Component, OnInit, inject} from '@angular/core';
import {AddBoxFacade} from './facades/add-box.facade';
import {AddBoxFormService} from './services/add-box.form.service';

@Component({
  selector: 'add-box',
  standalone: false,
  templateUrl: './add-box.component.html',
  styleUrls: ['./add-box.component.scss'],
  providers: [AddBoxFacade, AddBoxFormService]
})
export class AddBoxComponent implements OnInit {
  protected readonly facade = inject(AddBoxFacade);

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

  goToBack(): void {
    this.facade.goToBack();
  }

  checkCode(event: any): void {
    this.facade.checkCode(event);
  }

  save(): void {
    this.facade.save();
  }
}
