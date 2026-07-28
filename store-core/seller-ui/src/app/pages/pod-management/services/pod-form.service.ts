import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {validators} from '../../shared/validation/validators';
import {EndpointType, Pod} from '../../store-management/services/pod.service';

@Injectable()
export class PodFormService {
  private readonly fb = inject(FormBuilder);

  readonly endpointTypes = Object.values(EndpointType);

  readonly form: FormGroup = this.fb.group({
    name: ['', [Validators.required, Validators.pattern(validators.podName)]],
    endpoint: this.fb.group({
      endpoint: ['', [Validators.required, Validators.pattern(validators.url)]],
      type: ['', Validators.required]
    }),
    orgId: this.fb.group({
      id: ['']
    })
  });

  get name() {
    return this.form.get('name');
  }

  get endpointUrl() {
    return this.form.get('endpoint.endpoint');
  }

  patchForm(pod: Pod): void {
    this.form.patchValue(pod);
  }
}
