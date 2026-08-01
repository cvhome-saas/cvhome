import {Component} from '@angular/core';
import {PodFormComponent} from '../pod-form/pod-form.component';

@Component({
  selector: 'app-create-pod',
  standalone: true,
  imports: [PodFormComponent],
  templateUrl: './create-pod.component.html',
  styleUrls: ['./create-pod.component.scss']
})
export class CreatePodComponent {
}
