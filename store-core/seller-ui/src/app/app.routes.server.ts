import {RenderMode, ServerRoute} from "@angular/ssr";

export const serverRoutes: ServerRoute[] = [
  {
    path: "",
    renderMode: RenderMode.Server
  },
  {
    path: "signup",
    renderMode: RenderMode.Server
  },
  {
    path: "terms",
    renderMode: RenderMode.Server
  },
  {
    path: "privacy-policy",
    renderMode: RenderMode.Server
  }, {
    path: "external-logout-link",
    renderMode: RenderMode.Client
  },
  {
    path: "pages/**",
    renderMode: RenderMode.Client
  },
  {
    path: "subscription/**",
    renderMode: RenderMode.Client
  }
];
