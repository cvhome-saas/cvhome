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
  },
  {
    path: "pages/**",
    renderMode: RenderMode.Client
  },
  {
    path: "external-login-link",
    renderMode: RenderMode.Client
  },
  {
    path: "external-logout-link",
    renderMode: RenderMode.Client
  }
];
