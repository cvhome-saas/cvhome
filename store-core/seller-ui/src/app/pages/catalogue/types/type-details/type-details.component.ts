import {Component, OnInit, inject} from '@angular/core';
import {TypeDetailsFacade} from '../facades/type-details.facade';
import {TypeFormService} from '../services/type-form.service';

@Component({
  selector: 'ngx-type-details',
  standalone: false,
  templateUrl: './type-details.component.html',
  styleUrls: ['./type-details.component.scss'],
  providers: [TypeDetailsFacade, TypeFormService]
})
export class TypeDetailsComponent implements OnInit {
  protected readonly facade = inject(TypeDetailsFacade);

  ngOnInit(): void {
    this.facade.init();
  }
}
