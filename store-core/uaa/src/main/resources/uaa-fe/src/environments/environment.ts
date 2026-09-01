export const environment = {
  production: true,
  /*
   * Empty: uaa serves this app from its own `static/`, so every request is same-origin and the
   * admin API is reached at `/api/v1/admin/...` directly rather than through the gateway's `/uaa`
   * prefix. That prefix is what console-ui needs, and is the one thing the two configs differ on.
   */
  apiUrl: '',
  loginUrl: '/login',
  logoutUrl: '/logout',
};
