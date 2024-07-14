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
  message: string = 'By subscribing you will get newsleter, promotions adipisicing elit. Architecto beatae, asperiores tempore repudiandae saepe aspernatur unde voluptate sapiente quia ex.';
  email: string = '';

  public sub() {
    console.log("sub "+this.email)
    this.email = '';
  }
}
