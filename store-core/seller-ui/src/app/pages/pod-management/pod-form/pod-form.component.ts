import {Component, Input, OnInit, inject} from '@angular/core';
import {Pod} from '../../store-management/services/pod.service';
import {PodFormFacade} from '../facades/pod-form.facade';
import {PodFormService} from '../services/pod-form.service';

@Component({
  selector: 'ngx-pod-form',
  standalone: false,
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
