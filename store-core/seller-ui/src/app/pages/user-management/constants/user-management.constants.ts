export const PWD_PATTERN = '^(?=[^A-Z]*[A-Z])(?=[^a-z]*[a-z])(?=[^0-9]*[0-9]).{6,12}$';
export const EMAIL_PATTERN = '^([a-zA-Z0-9_\\-\\.]+)@([a-zA-Z0-9_\\-\\.]+)\\.([a-zA-Z]{2,5})$';

export const USER_DETAILS_SIDE_MENU_LINKS = [
  {
    id: '0',
    title: 'COMPONENTS.USER_DETAILS',
    key: 'COMPONENTS.USER_DETAILS',
    link: '/pages/user-management/user/{userId}',
  },
  {
    id: '1',
    title: 'COMPONENTS.CHANGE_PASSWORD',
    key: 'COMPONENTS.CHANGE_PASSWORD',
    link: '/pages/user-management/change-password/{userId}',
  }
];

export const NEW_USER_SIDE_MENU_LINKS = [
  {
    id: '0',
    title: 'COMPONENTS.USER_DETAILS',
    key: 'COMPONENTS.USER_DETAILS',
    link: '/pages/user-management/user',
  },
  {
    id: '1',
    title: 'COMPONENTS.CHANGE_PASSWORD',
    key: 'COMPONENTS.CHANGE_PASSWORD',
    link: '/pages/user-management/change-password',
  }
];
