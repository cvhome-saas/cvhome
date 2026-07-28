import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {ContactFacade} from './facades/contact.facade';
import {ContactFormService} from './services/contact-form.service';

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  providers: [ContactFacade, ContactFormService],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.css'
})
export class ContactComponent {
  protected readonly facade = inject(ContactFacade);

  title: string = 'Stay Tuned';
  d1: string = 'Contact us';
  d2: string = 'We are happy to help you';
  address: string = 'cairo festival city podium 1 New Cairo egypt';
  phone: string = '+20**********';
  email: string = 'me@asrevo.com';

  contact(): void {
    this.facade.submit();
  }
}
