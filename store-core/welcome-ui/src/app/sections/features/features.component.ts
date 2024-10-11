import {Component} from '@angular/core';
import {NgClass, NgForOf} from "@angular/common";

@Component({
  selector: 'app-features',
  standalone: true,
  imports: [
    NgForOf,
    NgClass
  ],
  templateUrl: './features.component.html',
  styleUrl: './features.component.css'
})
export class FeaturesComponent {
  title: string = 'What Makes Cvhome Different?'
  desc: string = 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Laborum obcaecati dignissimos quae quo ad iste ipsum officiis deleniti asperiores sit.'
  features: Feature[] = [
    {
      title:"Fully functional",
      desc:"Lorem ipsum dolor sit amet, consectetur adipisicing elit. Veritatis culpa expedita dignissimos.",
      img:"img/icon/featured-img/layers.png",
      fade:"fadeInLeft"
    },
    {
      title:"Live Chat",
      desc:"Lorem ipsum dolor sit amet, consectetur adipisicing elit. Veritatis culpa expedita dignissimos.",
      img:"img/icon/featured-img/speak.png",
      fade:"fadeInUp"
    },
    {
      title:"Secure Data",
      desc:"Lorem ipsum dolor sit amet, consectetur adipisicing elit. Veritatis culpa expedita dignissimos.",
      img:"img/icon/featured-img/lock.png",
      fade:"fadeInRight"
    }
  ];
}

interface Feature {
  title: string
  desc: string
  img: string
  fade:string
}
