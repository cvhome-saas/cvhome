import {Component} from '@angular/core';
import {NgFor, NgIf} from "@angular/common";

@Component({
  selector: 'app-asked-questions',
  standalone: true,
  imports: [NgFor,NgIf],
  templateUrl: './asked-questions.component.html',
  styleUrl: './asked-questions.component.css'
})
export class AskedQuestionsComponent {
  title: string = 'Frequently asked questions';
  desc: string = 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Laborum obcaecati dignissimos quae quo ad iste ipsum officiis deleniti asperiores sit.';
  questions: Question[] = [
    {
      question: "How to install sApp?",
      answer: "The point of using Lorem Ipsum is that it has a more-or-less normal distribution of letters, as opposed to using 'Content here, content here', making it look like readable English. Many desktop publishing packages and web page editors now use Lorem Ipsum as their default model text.",
      location: Location.LEFT
    },
    {
      question: "Can I get support from the Author?",
      answer: "Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source.",
      location: Location.LEFT
    },
    {
      question: "Do you have a free trail?",
      answer: "It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
      location: Location.LEFT
    },
    {
      question: "How can I edit my personal information?",
      answer: "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt.",
      location: Location.RIGHT
    },
    {
      question: "Contact form isn't working?",
      answer: "There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don't look even slightly believable. If you are going to use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden in the middle of text.",
      location: Location.RIGHT
    },
    {
      question: "Contact form isn't working?",
      answer: "There are many variations of passages of Lorem Ipsum available, but the majority have suffered alteration in some form, by injected humour, or randomised words which don't look even slightly believable. If you are going to use a passage of Lorem Ipsum, you need to be sure there isn't anything embarrassing hidden in the middle of text.",
      location: Location.RIGHT
    }
  ];

}

interface Question {
  question: string
  answer: string
  location: Location
}

enum Location {
  LEFT = 'LEFT', RIGHT = 'RIGHT'
}
