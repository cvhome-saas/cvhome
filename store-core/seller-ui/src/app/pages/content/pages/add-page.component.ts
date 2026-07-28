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

  ngOnInit(): void {
    this.facade.init();
  }
}
