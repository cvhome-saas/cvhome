import {Injectable, inject, signal} from '@angular/core';
import {Router} from '@angular/router';
import {PodFormService} from '../services/pod-form.service';
import {Pod, PodService} from '../../store-management/services/pod.service';
import {ErrorService} from '../../shared/services/error.service';

@Injectable()
export class PodFormFacade {
  readonly formService = inject(PodFormService);
  private readonly podService = inject(PodService);
  private readonly router = inject(Router);
  private readonly errorService = inject(ErrorService);

  readonly loader = signal<boolean>(false);

  private podData: Pod | null = null;

  init(pod: Pod | null): void {
    this.podData = pod || null;
    if (this.podData) {
      this.formService.patchForm(this.podData);
    }
  }

  save(): void {
    if (this.formService.form.invalid) return;

    this.loader.set(true);
    const podToSend = this.toPod(this.formService.form.value);

    const request$ = this.podData?.id?.id
      ? this.podService.update(this.podData.id.id, {...podToSend, id: this.podData.id})
      : this.podService.create(podToSend);

    request$.subscribe({
      next: () => {
        this.loader.set(false);
        this.goToBack();
      },
      error: (err) => {
        this.loader.set(false);
        this.errorService.error('ERROR.SYSTEM_ERROR', err);
      }
    });
  }

  goToBack(): void {
    this.router.navigate(['pages/pod-management/list']);
  }

  private toPod(formValue: Partial<Pod>): Pod {
    // Form validators already require name/shortenPodId/endpoint before save() calls this.
    const pod = {...formValue} as Pod;
    if (!pod.orgId || !pod.orgId.id || pod.orgId.id.trim() === '') {
      delete pod.orgId;
    }
    return pod;
  }
}
