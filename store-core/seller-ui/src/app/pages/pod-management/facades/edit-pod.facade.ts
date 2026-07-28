import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {switchMap} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Pod, PodService} from '../../store-management/services/pod.service';
import {ErrorService} from '../../shared/services/error.service';

@Injectable()
export class EditPodFacade {
  private readonly route = inject(ActivatedRoute);
  private readonly podService = inject(PodService);
  private readonly errorService = inject(ErrorService);

  readonly pod = signal<Pod | null>(null);

  init(destroyRef: DestroyRef): void {
    this.route.params.pipe(
      switchMap((params) => this.podService.getPod(params['id'])),
      takeUntilDestroyed(destroyRef)
    ).subscribe({
      next: (pod) => this.pod.set(pod),
      error: (err) => this.errorService.error('ERROR.SYSTEM_ERROR', err)
    });
  }
}
