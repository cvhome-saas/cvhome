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
  title = 'Subscribe to get updates';
  message = 'By subscribing you will get newsleter, promotions';
  email = '';

  public sub() {
    this.email = '';
  }
}
