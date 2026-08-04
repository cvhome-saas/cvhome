import {NbMenuItem} from '@nebular/theme';

export const STORE_SELECT_DISABLED_ROUTE_PREFIXES = [
  '/pages/store-management',
  '/pages/subscription-and-usage',
  '/pages/org-management',
];

export enum HeaderMenuAction {
  Profile = 'Profile',
  Logout = 'Log out',
}

export const HEADER_USER_MENU: NbMenuItem[] = [
  {title: HeaderMenuAction.Profile},
  {title: HeaderMenuAction.Logout},
];
