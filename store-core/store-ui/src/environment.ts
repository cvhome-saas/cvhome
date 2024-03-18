export const environment = {
  production: false,
  // USER_INFO_URL: '/realms/cvhome/protocol/openid-connect/userinfo',
  USER_INFO_URL: '/api/v1/auth/current',
  ACCOUNT_URL: '/realms/cvhome/account',
  LOGIN_URL: '/oauth2/authorization/keycloak',
  REGISTER_URL: '/oauth2/authorization/keycloak?SSS',
  CLIENT_APP: 'web-app',
};
