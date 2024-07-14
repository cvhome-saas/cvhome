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
  d1: string = 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Laborum obcaecati dignissimos quae quo ad iste ipsum officiis deleniti asperiores sit.';
  d2: string = 'Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old.';
  address: string = 'Vestibulum nulla libero, convallis, tincidunt suscipit diam, DC 2002';
  phone: string = '+1 230 456 789-012 345 6789';
  email: string = 'email@email.com';
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
