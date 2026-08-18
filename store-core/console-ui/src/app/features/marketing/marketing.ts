import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import type {ContactTopicId} from '@models/marketing';
import {Icon} from '@shared/ui/icon/icon';
import {ContactFormService} from './services/contact-form.service';
import {MarketingFacade} from './facades/marketing.facade';

@Component({
  selector: 'app-marketing',
  imports: [ReactiveFormsModule, Icon, TranslocoDirective],
  templateUrl: './marketing.html',
  styleUrl: './marketing.css',
})
export class Marketing {
  protected readonly facade = inject(MarketingFacade);
  protected readonly contactForm = inject(ContactFormService).create();

  // Brand name, not translated — same as Stripe/PayPal elsewhere in the app.
  protected readonly brandName = 'cvhome';

  protected price(plan: {monthlyPrice: number}): number {
    return this.facade.price(plan);
  }

  protected selectTopic(topic: ContactTopicId): void {
    this.facade.selectedTopic.set(topic);
    this.contactForm.controls.topic.setValue(topic);
  }

  protected sendMessage(): void {
    if (this.contactForm.invalid) {
      this.contactForm.markAllAsTouched();
      return;
    }
    this.facade.sendMessage(this.contactForm.getRawValue());
  }
}
