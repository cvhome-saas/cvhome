import {MenuItem} from './menu-item';
import {Roles} from "./shared/models/roles";

const CanAccessSysAdminHome = (roles: Roles) => {
  return roles.isSuperAdmin;
};


export const MENU_ITEMS: MenuItem[] =
  [
    {
      title: 'COMPONENTS.HOME',
      key: 'COMPONENTS.HOME',
      icon: 'home',
      guards: [CanAccessSysAdminHome],
      link: '/home',
      home: true,
    },
    {
      title: 'COMPONENTS.CLIENT_MANAGEMENT',
      key: 'COMPONENTS.CLIENT_MANAGEMENT',
      icon: 'user-plus',
      guards: [CanAccessSysAdminHome],
      children: [
        {
          title: 'COMPONENTS.CLIENT_LIST',
          key: 'COMPONENTS.CLIENT_LIST',
          link: '/home/clients',
          hidden: false
        },
      ],
    },
    {
      title: 'COMPONENTS.USER_MANAGEMENT',
      key: 'COMPONENTS.USER_MANAGEMENT',
      icon: 'user-plus',
      guards: [CanAccessSysAdminHome],
      children: [
        {
          title: 'COMPONENTS.USER_LIST',
          key: 'COMPONENTS.USER_LIST',
          link: '/home/users',
          hidden: false
        },
      ],
    },
    {
      title: 'COMPONENTS.ROLE_MANAGEMENT',
      key: 'COMPONENTS.ROLE_MANAGEMENT',
      icon: 'user-plus',
      guards: [CanAccessSysAdminHome],
      children: [
        {
          title: 'COMPONENTS.ROLE_LIST',
          key: 'COMPONENTS.ROLE_LIST',
          link: '/home/roles',
          hidden: false
        },
      ],
    }
  ];

