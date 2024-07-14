import {Component} from '@angular/core';
import {NgForOf} from "@angular/common";

@Component({
  selector: 'app-review',
  standalone: true,
  imports: [
    NgForOf
  ],
  templateUrl: './review.component.html',
  styleUrl: './review.component.css'
})
export class ReviewComponent {
  title: string = 'What our customers are saying';
  desc: string = 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Laborum bcaecati dignissimos quae quo ad iste ipsum officiis deleniti asperiores sit.';
  reviews: Review[] = [{
    stars: 5,
    subject: "Excellent service & support!",
    review: "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quis nam id facilis, provident doloremque placeat eveniet molestias laboriosam. Optio, esse.",
    creator: {
      name: "Junaid Hasan",
      position: "CEO",
      company: "Themeland",
      img: "img/avatar/avatar-1.png"
    }
  }, {
    stars: 6,
    subject: "Excellent service & support!",
    review: "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quis nam id facilis, provident doloremque placeat eveniet molestias laboriosam. Optio, esse.",
    creator: {
      name: "Junaid Hasan",
      position: "CEO",
      company: "Themeland",
      img: "img/avatar/avatar-1.png"
    }
  }, {
    stars: 4,
    subject: "Excellent service & support!",
    review: "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quis nam id facilis, provident doloremque placeat eveniet molestias laboriosam. Optio, esse.",
    creator: {
      name: "Junaid Hasan",
      position: "CEO",
      company: "Themeland",
      img: "img/avatar/avatar-1.png"
    }
  }];
}

interface Review {
  stars: number
  subject: string
  review: string
  creator: Creator
}

interface Creator {
  name: string
  position: string
  company: string
  img: string
}
