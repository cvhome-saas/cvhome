import {Component, DestroyRef, OnInit, inject} from '@angular/core';
import {EditPodFacade} from '../facades/edit-pod.facade';
import {PodFormComponent} from '../pod-form/pod-form.component';

@Component({
  selector: 'app-edit-pod',
  standalone: true,
  imports: [PodFormComponent],
  templateUrl: './edit-pod.component.html',
  styleUrls: ['./edit-pod.component.scss'],
  providers: [EditPodFacade]
})
export class EditPodComponent implements OnInit {
  protected readonly facade = inject(EditPodFacade);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.facade.init(this.destroyRef);
  }
}
