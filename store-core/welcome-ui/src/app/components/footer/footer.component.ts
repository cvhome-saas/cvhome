import {Component} from '@angular/core';
import {NgClass, NgForOf} from "@angular/common";

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [
    NgForOf,
    NgClass
  ],
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css'
})
export class FooterComponent {
  message: string = 'Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quis non, fugit totam vel laboriosam vitae.'
  socialLinks: SocialLink[] = [
    {
      class: "facebook",
      icon: "fa-facebook-f",
      url: "https://facebook.com"
    },
    {
      class: "twitter",
      icon: "fa-twitter",
      url: "https://twitter.com"
    },
    {
      class: "google-plus",
      icon: "fa-google-plus-g",
      url: "https://google.com"
    },
    {
      class: "vine",
      icon: "fa-vine",
      url: "https://vine.com"
    },
  ];
  usefulLinks: UsefulLink[] = [
    {
      name: "Home",
      url: "#"
    },
    {
      name: "About Us",
      url: "#"
    },
    {
      name: "Services",
      url: "#"
    },
    {
      name: "Blog",
      url: "#"
    },
    {
      name: "Contact",
      url: "#"
    },
  ];
  productHelp: UsefulLink[] = [
    {
      name: "FAQ",
      url: "#"
    },
    {
      name: "Privacy Policy",
      url: "#"
    },
    {
      name: "Support",
      url: "#"
    },
    {
      name: "Terms &amp; Conditions",
      url: "#"
    },
    {
      name: "Contact",
      url: "#"
    },
  ];
  downloadLinks: DownloadLink[] = [
    {
      icon: "img/icon/google-play-black.png",
      url: "#"
    },
    {
      icon: "img/icon/app-store-black.png",
      url: "#"
    }
  ];
  creator: Creator = {
    name: "ashraf",
    link: "#"
  }
  copyrights:string='Copyrights 2022 sApp All rights reserved.'
}

interface SocialLink {
  class: string
  icon: string
  url: string
}

interface UsefulLink {
  name: string
  url: string
}

interface DownloadLink {
  icon: string
  url: string
}

interface Creator {
  name: string
  link: string
}
