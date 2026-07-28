export interface SocialLink {
  class: string;
  icon: string;
  url: string;
}

export interface UsefulLink {
  name: string;
  url: string;
}

export interface DownloadLink {
  icon: string;
  url: string;
}

export interface Creator {
  name: string;
  link: string;
}

export const FOOTER_SOCIAL_LINKS: SocialLink[] = [
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

export const FOOTER_USEFUL_LINKS: UsefulLink[] = [
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
    url: "#contact"
  },
];

export const FOOTER_PRODUCT_HELP_LINKS: UsefulLink[] = [
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
    name: "Terms & Conditions",
    url: "#"
  },
  {
    name: "Contact",
    url: "#contact"
  },
];

export const FOOTER_DOWNLOAD_LINKS: DownloadLink[] = [
  {
    icon: "img/icon/google-play-black.png",
    url: "#"
  },
  {
    icon: "img/icon/app-store-black.png",
    url: "#"
  }
];

export const FOOTER_CREATOR: Creator = {
  name: "me@asrevo.com",
  link: "#"
};
