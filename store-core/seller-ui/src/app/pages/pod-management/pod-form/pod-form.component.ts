import {Component, Input, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {EndpointType, Pod, PodService} from '../../store-management/services/pod.service';
import {Router} from "@angular/router";

@Component({
  selector: 'ngx-pod-form',
  standalone: false,
  templateUrl: './pod-form.component.html',
  styleUrls: ['./pod-form.component.scss']
})
export class PodFormComponent implements OnInit {
  @Input() title: string;
  @Input() pod: Pod;
  loader = false

  form: FormGroup;
  endpointTypes = Object.values(EndpointType);
  // Simple URL regex
  urlRegex = /^(http|https):\/\/[^ "]+$/;
  // Name regex: alphanumeric and hyphen
  nameRegex = /^[a-zA-Z0-9-]+$/;

  constructor(private fb: FormBuilder, private podService: PodService, private router: Router) {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.pattern(this.nameRegex)]],
      endpoint: this.fb.group({
        endpoint: ['', [Validators.required, Validators.pattern(this.urlRegex)]],
        type: ['', Validators.required]
      }),
      orgId: this.fb.group({
        id: ['']
      })
    });
  }

  ngOnInit(): void {
    if (this.pod) {
      this.form.patchValue(this.pod);
    }
  }

  save() {
    if (this.form.valid) {
      const podToSend = this.toPod(this.form.value);
      if (this.pod && this.pod.id && this.pod.id.id) {
        // Ensure ID is preserved if updating
        podToSend.id = this.pod.id;
        this.podService.update(this.pod.id.id, podToSend).subscribe(() => {
          this.router.navigate(['/pages/pod-management/list']);
        });
      } else {
        this.podService.create(podToSend).subscribe(() => {
          this.router.navigate(['/pages/pod-management/list']);
        });
      }

    }
  }

  goToBack() {
    this.router.navigate(['pages/pod-management/list']);
  }

  toPod(formValue: any): Pod {
    const pod: Pod = {...formValue};
    if (!pod.orgId || !pod.orgId.id || pod.orgId.id.trim() === '') {
      delete pod.orgId;
    }
    return pod;
  }

  get name() {
    return this.form.get('name');
  }

  get endpointUrl() {
    return this.form.get('endpoint.endpoint');
  }
}
