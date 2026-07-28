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

  ngOnInit(): void {
    this.facade.init();
  }
}
