import {Component, Input, OnInit, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';
import {
  NbButtonModule,
  NbCardModule,
  NbInputModule,
  NbOptionModule,
  NbSelectModule,
  NbSpinnerModule
} from '@nebular/theme';
import {Pod} from '../../store-management/services/pod.service';
import {PodFormFacade} from '../facades/pod-form.facade';
import {PodFormService} from '../services/pod-form.service';

@Component({
  selector: 'ngx-pod-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    TranslateModule,
    NbButtonModule,
    NbCardModule,
    NbInputModule,
    NbOptionModule,
    NbSelectModule,
    NbSpinnerModule
  ],
  templateUrl: './pod-form.component.html',
  styleUrls: ['./pod-form.component.scss'],
  providers: [PodFormFacade, PodFormService]
})
export class PodFormComponent implements OnInit {
  @Input() title: string;
  @Input() pod: Pod;

  protected readonly facade = inject(PodFormFacade);

  ngOnInit(): void {
    this.facade.init(this.pod);
  }
}
