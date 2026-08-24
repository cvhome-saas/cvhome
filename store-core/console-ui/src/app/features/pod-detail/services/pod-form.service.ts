import {Injectable, inject} from '@angular/core';
import {FormControl, FormGroup, NonNullableFormBuilder, Validators} from '@angular/forms';

import type {PodDetail} from '@models/platform';
import type {EndpointType} from '@models/pod';

/**
 * A pod's name, as seller-ui's `validators.podName` spelled it: letters, digits and hyphens.
 *
 * Carried over rather than invented, because it is not arbitrary — `pod_name_uq` makes the name a
 * unique key and `PodEndpoint` values get built into hostnames from the shortened id, so a name with
 * a space in it is a name that reads badly everywhere it appears. The server enforces uniqueness and
 * nothing else; this is the console's own shape rule.
 */
export const POD_NAME_PATTERN = /^[a-zA-Z0-9-]+$/;

/**
 * An endpoint, as the registry stores it.
 *
 * `PodEntity.endpoint` is a plain string and the server validates nothing about it, but the gateway
 * builds routes from it — an endpoint that is not an absolute URL is a route that resolves nowhere.
 */
export const POD_ENDPOINT_PATTERN = /^https?:\/\/\S+$/;

export type PodForm = FormGroup<{
  name: FormControl<string>;
  endpoint: FormControl<string>;
  endpointType: FormControl<EndpointType>;
  /** The owning organization's id, or `''` for a shared pod. Disabled when editing — see below. */
  orgId: FormControl<string>;
}>;

/**
 * The pod's create/edit form.
 *
 * Built here rather than in the component or the facade, per `ARCHITECTURE.md` §5.
 *
 * **The owner is settable exactly once.** `PodEntity.newEntity` derives visibility from whether an
 * organization was named, and `PodServiceImpl.update` reads only `name` and `endpoint` off the body
 * — so on an edit the control is disabled rather than merely ignored: `form.value` omits disabled
 * controls, which means there is no route by which an edit sends a value the server would drop. See
 * lessons.md, "Pods — visibility, region, capacity and owner cannot be edited".
 *
 * Region and capacity have no control at all, in either mode. They are columns on `pod_registry.pod`
 * that no endpoint writes, so a field for them would be a field that never took effect.
 */
@Injectable({providedIn: 'root'})
export class PodFormService {
  private readonly fb = inject(NonNullableFormBuilder);

  build(mode: 'create' | 'edit'): PodForm {
    const form: PodForm = this.fb.group({
      name: this.fb.control('', [Validators.required, Validators.pattern(POD_NAME_PATTERN)]),
      endpoint: this.fb.control('', [Validators.required, Validators.pattern(POD_ENDPOINT_PATTERN)]),
      endpointType: this.fb.control<EndpointType>('EXTERNAL'),
      orgId: this.fb.control(''),
    });

    if (mode === 'edit') {
      form.controls.orgId.disable();
    }
    return form;
  }

  /** Fills the edit form from the view the detail page already loaded. */
  patchFrom(form: PodForm, pod: PodDetail): void {
    form.reset({
      name: pod.name,
      endpoint: pod.endpoint,
      endpointType: (pod.endpointType || 'EXTERNAL') as EndpointType,
      orgId: pod.orgId ?? '',
    });
  }
}
