import {Component} from '@angular/core';
import {NgClass} from "@angular/common";
import {FEATURES} from './constants/features.constants';

@Component({
  selector: 'app-features',
  standalone: true,
  imports: [
    NgClass
  ],
  templateUrl: './features.component.html',
  styleUrl: './features.component.css'
})
export class FeaturesComponent {
  title = 'What Makes Cvhome Different?';
  desc = 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Laborum obcaecati dignissimos quae quo ad iste ipsum officiis deleniti asperiores sit.';
  readonly features = FEATURES;
}
