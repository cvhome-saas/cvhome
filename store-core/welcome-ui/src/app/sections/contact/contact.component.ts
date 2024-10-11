import {Component} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from "@angular/forms";

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './contact.component.html',
  styleUrl: './contact.component.css'
})
export class ContactComponent {
  title: string = 'Stay Tuned';
  d1: string = 'Contact us';
  d2: string = 'We are happy to help you';
  address: string = 'cairo festival city podium 1 New Cairo egypt';
  phone: string = '+20**********';
  email: string = 'me@asrevo.com';
  contactForm = new FormGroup({
    name: new FormControl(''),
    email: new FormControl(''),
    subject: new FormControl(''),
    message: new FormControl(''),
  });

  contact() {
    if (this.contactForm.valid) {
      const message = this.contactForm.value as ContactMessage;
      console.log(message)
      this.contactForm.reset({})
    }
  }
}

interface ContactMessage {
  name: string
  email: string
  subject: string
  message: string
}
