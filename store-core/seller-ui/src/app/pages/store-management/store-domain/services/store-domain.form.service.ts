import {Injectable, inject} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {validators} from 'seller-core';
import {DnsCheckService} from 'seller-core/stores';
import {dnsPointsToPodValidatorFactory, StoreDomainComponentValidatorContext} from '../store-domain.validator';

@Injectable()
export class StoreDomainFormService {
  private readonly fb = inject(FormBuilder);
  private readonly dnsCheckService = inject(DnsCheckService);

  createForm(context: StoreDomainComponentValidatorContext): FormGroup {
    return this.fb.group({
      domain: [
        '',
        {
          validators: [Validators.required, Validators.pattern(validators.domainPattern)],
          asyncValidators: [dnsPointsToPodValidatorFactory(context, this.dnsCheckService)],
          updateOn: 'blur'
        }
      ],
    });
  }
}
