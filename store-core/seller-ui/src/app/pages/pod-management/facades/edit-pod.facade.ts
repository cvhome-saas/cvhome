import {Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {Pod, PodService} from '../../store-management/services/pod.service';
import {ErrorService} from '../../shared/services/error.service';

@Injectable()
export class EditPodFacade {
  private readonly route = inject(ActivatedRoute);
  private readonly podService = inject(PodService);
  private readonly errorService = inject(ErrorService);

  readonly pod = signal<Pod | null>(null);

  init(): void {
    const podId = this.route.snapshot.paramMap.get('id');
    if (!podId) return;

    this.podService.getPod(podId).subscribe({
      next: (pod) => this.pod.set(pod),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }
}
