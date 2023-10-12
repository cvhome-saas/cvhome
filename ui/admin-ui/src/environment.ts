export const environment = {
  production: false,
  USER_INFO_URL: '/realms/cvhome/protocol/openid-connect/userinfo',
  ACCOUNT_URL: '/realms/cvhome/account',
  LOGIN_URL: '/oauth2/authorization/keycloak',
  REGISTER_URL: '/oauth2/authorization/keycloak?SSS',
  CLIENT_APP: 'web-app',
  BASE_DOMAIN: 'gateway.com',
  WWW_BASE_DOMAIN: 'www.gateway.com',
  ALL_BASE_DOMAINS: ['gateway.com', 'www.gateway.com']
};
