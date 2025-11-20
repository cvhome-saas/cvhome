import {Component} from '@angular/core';
import {FormsModule} from "@angular/forms";

@Component({
  selector: 'app-subscribe',
  standalone: true,
  imports: [
    FormsModule
  ],
  templateUrl: './subscribe.component.html',
  styleUrl: './subscribe.component.css'
})
export class SubscribeComponent {
  title: string = 'Subscribe to get updates';
  message: string = 'By subscribing you will get newsleter, promotions';
  email: string = '';

  public sub() {
    console.log("sub " + this.email)
    this.email = '';
  }
}
