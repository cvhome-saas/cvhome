export interface Feature {
  title: string;
  desc: string;
  img: string;
  fade: string;
}

export const FEATURES: Feature[] = [
  {
    title: "Fully functional",
    desc: "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Veritatis culpa expedita dignissimos.",
    img: "img/icon/featured-img/layers.png",
    fade: "fadeInLeft"
  },
  {
    title: "Live Chat",
    desc: "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Veritatis culpa expedita dignissimos.",
    img: "img/icon/featured-img/speak.png",
    fade: "fadeInUp"
  },
  {
    title: "Secure Data",
    desc: "Lorem ipsum dolor sit amet, consectetur adipisicing elit. Veritatis culpa expedita dignissimos.",
    img: "img/icon/featured-img/lock.png",
    fade: "fadeInRight"
  }
];
