import {Component} from '@angular/core';
import {NgClass, NgForOf} from "@angular/common";
import {
  FOOTER_CREATOR,
  FOOTER_DOWNLOAD_LINKS,
  FOOTER_PRODUCT_HELP_LINKS,
  FOOTER_SOCIAL_LINKS,
  FOOTER_USEFUL_LINKS
} from './constants/footer.constants';

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
  message: string = 'Follow us.';
  readonly socialLinks = FOOTER_SOCIAL_LINKS;
  readonly usefulLinks = FOOTER_USEFUL_LINKS;
  readonly productHelp = FOOTER_PRODUCT_HELP_LINKS;
  readonly downloadLinks = FOOTER_DOWNLOAD_LINKS;
  readonly creator = FOOTER_CREATOR;
  copyrights: string = 'Copyrights 2024 Cvhome All rights reserved.';
}
