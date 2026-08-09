import {DestroyRef, Injectable, inject, signal} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {switchMap} from 'rxjs';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {Pod, PodService} from 'seller-core/stores';
import {ApiErrorService} from 'seller-core';

@Injectable()
export class EditPodFacade {
  private readonly route = inject(ActivatedRoute);
  private readonly podService = inject(PodService);
  private readonly apiErrors = inject(ApiErrorService);

  readonly pod = signal<Pod | null>(null);

  init(destroyRef: DestroyRef): void {
    this.route.params.pipe(
      switchMap((params) => this.podService.getPod(params['id'])),
      takeUntilDestroyed(destroyRef)
    ).subscribe({
      next: (pod) => this.pod.set(pod),
      error: (err) => this.apiErrors.notify(err)
    });
  }
}
