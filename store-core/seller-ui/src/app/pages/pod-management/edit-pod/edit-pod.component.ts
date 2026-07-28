import {Component, OnInit, inject} from '@angular/core';
import {EditPodFacade} from '../facades/edit-pod.facade';

@Component({
  selector: 'app-edit-pod',
  standalone: false,
  templateUrl: './edit-pod.component.html',
  styleUrls: ['./edit-pod.component.scss'],
  providers: [EditPodFacade]
})
export class EditPodComponent implements OnInit {
  protected readonly facade = inject(EditPodFacade);

  ngOnInit(): void {
    this.facade.init();
  }
}
