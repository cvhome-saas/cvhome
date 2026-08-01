import {Component, inject} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {SignUpFormFacade} from './facades/sign-up-form.facade';
import {SignUpFormService} from './services/sign-up-form.service';

@Component({
  selector: 'app-sign-up-form',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  providers: [SignUpFormFacade, SignUpFormService],
  templateUrl: './sign-up-form.component.html',
  styleUrl: './sign-up-form.component.css'
})
export class SignUpFormComponent {
  protected readonly facade = inject(SignUpFormFacade);
  title = 'Sign Up';

  signUp(): void {
    this.facade.signUp();
  }
}
