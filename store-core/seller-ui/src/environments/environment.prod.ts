/**
 * @license
 * Copyright Akveo. All Rights Reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */
export const environment = {
  production: true,
  //MARKETPLACE | BTB | STANDARD
  mode: 'STANDARD',
  //API URL
  //apiUrl: "http://localhost:8080/api/api",
  apiUrl: '',
  shippingApi: '',
  client: {
    language: {
      default: 'en',
      array: [
        'en',
        'fr',
        'ar',
        'es',
        'ru',
      ],
    },
  },
  LOGIN_URL: "/oauth2/authorization/uaa",
  LOGOUT_URL: "/logout"

};
