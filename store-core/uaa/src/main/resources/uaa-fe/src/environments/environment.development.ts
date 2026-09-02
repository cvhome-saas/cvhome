export const environment = {
  production: false,
  /*
   * `ng serve` runs on its own port with no uaa behind it, so requests are proxied to the running
   * stack. Point this at uaa's own origin when driving the admin screens from the dev server.
   */
  apiUrl: '',
  loginUrl: '/login',
  logoutUrl: '/logout',
};
