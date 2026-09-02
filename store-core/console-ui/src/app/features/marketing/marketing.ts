import {Component, OnInit, inject} from '@angular/core';
import {CurrencyPipe} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {TranslocoDirective} from '@jsverse/transloco';

import type {ContactTopicId} from '@models/marketing';
import {Icon, FormField, TextField, TextareaField} from '@cvhome-saas/ui-kit/ui';
import {ContactFormService} from './services/contact-form.service';
import {MarketingFacade} from './facades/marketing.facade';

@Component({
  providers: [MarketingFacade],
  selector: 'app-marketing',
  imports: [
    FormField,
    TextField,
    TextareaField,CurrencyPipe, ReactiveFormsModule, Icon, TranslocoDirective],
  templateUrl: './marketing.html',
  styleUrls: ['../../shared/styles/field.css', './marketing.css'],
})
export class Marketing implements OnInit {
  protected readonly facade = inject(MarketingFacade);
  protected readonly contactForm = inject(ContactFormService).create();

  // Brand name, not translated — same as Stripe/PayPal elsewhere in the app.
  protected readonly brandName = 'cvhome';

  ngOnInit(): void {
    this.facade.loadPlans();
  }

  protected selectTopic(topic: ContactTopicId): void {
    this.facade.selectedTopic.set(topic);
    this.contactForm.controls.topic.setValue(topic);
  }

}
