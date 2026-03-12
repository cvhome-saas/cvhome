import {Component} from '@angular/core';

@Component({
  selector: 'app-footer',
  imports: [],
  templateUrl: './footer.component.html'
})
export class FooterComponent {
  creator: Creator = {
    name: "me@asrevo.com",
    link: "#"
  }
  copyrights: string = 'Copyrights 2024 Cvhome All rights reserved.'
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
