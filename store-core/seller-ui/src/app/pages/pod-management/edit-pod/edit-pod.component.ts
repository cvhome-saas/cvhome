import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {PodService, Pod} from '../../store-management/services/pod.service';

@Component({
  selector: 'app-edit-pod',
  standalone: false,
  templateUrl: './edit-pod.component.html',
  styleUrls: ['./edit-pod.component.scss']
})
export class EditPodComponent implements OnInit {
  pod: Pod;

  constructor(
    private podService: PodService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    const podId = this.route.snapshot.paramMap.get('id');
    if (podId) {
      this.podService.getPod(podId).subscribe(pod => {
        this.pod = pod;
      });
    }
  }
}
