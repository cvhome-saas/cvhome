/*
import {NbMenuItem} from '@nebular/theme';

export const MENU_ITEMS: NbMenuItem[] = [
  {
    title: 'Store-Manager',
    icon: 'shopping-cart-outline',
    link: '/pages/store-manager',
    home: true,
  },
  {
    title: 'Managers',
    icon: 'home-outline',
    link: '/pages/store-manager/managers',
  },
  {
    title: 'Catalog',
    icon: 'home-outline',
    children:[
      {
        title: "Category",
        icon: 'home-outline',
        link: '/pages/store-manager/category',
      },
      {
        title: "Products",
        icon: 'home-outline',
        link: '/pages/store-manager/products',
      }
    ]
  },
  {
    title: 'LEARN',
    group: true,
    children: [
      {
        title: 'FEATURES',
        group: true,
      },
      {
        title: 'Layout',
        icon: 'layout-outline',
        children: [
          {
            title: 'Stepper',
            link: '/pages/layout/stepper',
          },
          {
            title: 'List',
            link: '/pages/layout/list',
          },
          {
            title: 'Infinite List',
            link: '/pages/layout/infinite-list',
          },
          {
            title: 'Accordion',
            link: '/pages/layout/accordion',
          },
          {
            title: 'Tabs',
            pathMatch: 'prefix',
            link: '/pages/layout/tabs',
          },
        ],
      },
      {
        title: 'Forms',
        icon: 'edit-2-outline',
        children: [
          {
            title: 'Form Inputs',
            link: '/pages/forms/inputs',
          },
          {
            title: 'Form Layouts',
            link: '/pages/forms/layouts',
          },
          {
            title: 'Buttons',
            link: '/pages/forms/buttons',
          },
          {
            title: 'Datepicker',
            link: '/pages/forms/datepicker',
          },
        ],
      },
      {
        title: 'UI Features',
        icon: 'keypad-outline',
        link: '/pages/ui-features',
        children: [
          {
            title: 'Grid',
            link: '/pages/ui-features/grid',
          },
          {
            title: 'Icons',
            link: '/pages/ui-features/icons',
          },
          {
            title: 'Typography',
            link: '/pages/ui-features/typography',
          },
          {
            title: 'Animated Searches',
            link: '/pages/ui-features/search-fields',
          },
        ],
      },
      {
        title: 'Modal & Overlays',
        icon: 'browser-outline',
        children: [
          {
            title: 'Dialog',
            link: '/pages/modal-overlays/dialog',
          },
          {
            title: 'Window',
            link: '/pages/modal-overlays/window',
          },
          {
            title: 'Popover',
            link: '/pages/modal-overlays/popover',
          },
          {
            title: 'Toastr',
            link: '/pages/modal-overlays/toastr',
          },
          {
            title: 'Tooltip',
            link: '/pages/modal-overlays/tooltip',
          },
        ],
      },
      {
        title: 'Extra Components',
        icon: 'message-circle-outline',
        children: [
          {
            title: 'Calendar',
            link: '/pages/extra-components/calendar',
          },
          {
            title: 'Progress Bar',
            link: '/pages/extra-components/progress-bar',
          },
          {
            title: 'Spinner',
            link: '/pages/extra-components/spinner',
          },
          {
            title: 'Alert',
            link: '/pages/extra-components/alert',
          },
          {
            title: 'Calendar Kit',
            link: '/pages/extra-components/calendar-kit',
          },
          {
            title: 'Chat',
            link: '/pages/extra-components/chat',
          },
        ],
      },
      {
        title: 'Maps',
        icon: 'map-outline',
        children: [
          {
            title: 'Google Maps',
            link: '/pages/maps/gmaps',
          },
          {
            title: 'Leaflet Maps',
            link: '/pages/maps/leaflet',
          },
          {
            title: 'Bubble Maps',
            link: '/pages/maps/bubble',
          },
          {
            title: 'Search Maps',
            link: '/pages/maps/searchmap',
          },
        ],
      },
      {
        title: 'Charts',
        icon: 'pie-chart-outline',
        children: [
          {
            title: 'Echarts',
            link: '/pages/charts/echarts',
          },
          {
            title: 'Charts.js',
            link: '/pages/charts/chartjs',
          },
          {
            title: 'D3',
            link: '/pages/charts/d3',
          },
        ],
      },
      {
        title: 'Editors',
        icon: 'text-outline',
        children: [
          {
            title: 'TinyMCE',
            link: '/pages/editors/tinymce',
          },
          {
            title: 'CKEditor',
            link: '/pages/editors/ckeditor',
          },
        ],
      },
      {
        title: 'Tables & Data',
        icon: 'grid-outline',
        children: [
          {
            title: 'Smart Table',
            link: '/pages/tables/smart-table',
          },
          {
            title: 'Tree Grid',
            link: '/pages/tables/tree-grid',
          },
        ],
      },
      {
        title: 'Miscellaneous',
        icon: 'shuffle-2-outline',
        children: [
          {
            title: '404',
            link: '/pages/miscellaneous/404',
          },
        ],
      },
      {
        title: 'Auth',
        icon: 'lock-outline',
        children: [
          {
            title: 'Login',
            link: '/auth/login',
          },
          {
            title: 'Register',
            link: '/auth/register',
          },
          {
            title: 'Request Password',
            link: '/auth/request-password',
          },
          {
            title: 'Reset Password',
            link: '/auth/reset-password',
          },
        ],
      },
    ]
  },

];
*/


import {MenuItem} from './menu-item';
import {Roles} from "../shared/domain/roles";

/**
 * Security
 * superadmin
 * adminretail
 * admin
 * other admins
 */

const IsAccessToOrder = (roles: Roles) => {
  return roles.canAccessToOrder;
};

const IsSuperadmin = (roles: Roles) => {
  return roles.isSuperadmin;
};

const IsAdmin = (roles: Roles) => {
  if (
    roles.isAdmin ||
    roles.isAdminRetail
  ) {
    return true;
  } else {
    return false;
  }
};

const IsAdminCatalogue = (roles: Roles) => {
  return roles.isAdminCatalogue;
};

const IsAdminStore = (roles: Roles) => {
  return roles.isAdminStore;
};

const IsAdminOrder = (roles: Roles) => {
  return roles.isAdminOrder;
};

const IsAdminContent = (roles: Roles) => {
  return roles.isAdminContent;
};

const IsCustomer = (roles: Roles) => {
  return roles.isCustomer;
};

const isCategoryManagementVisible = (roles: Roles) => {
  return IsAdminRetail || IsAdmin
}

const IsAdminRetail = (roles: Roles) => {
  if (
    roles.isSuperadmin ||
    roles.isAdminRetail ||
    roles.isAdmin
  ) {
    return true;
  } else {
    return false;
  }
};

const IsOrderManagementVisible = (roles: Roles) => {
  if (
    roles.isSuperadmin ||
    roles.isAdminRetail ||
    roles.isAdminOrder ||
    roles.isAdmin
  ) {
    return true;
  } else {
    return false;
  }
};


export const MENU_ITEMS: MenuItem[] =
  [
    {
      title: 'COMPONENTS.HOME',
      key: 'COMPONENTS.HOME',
      icon: 'home',
      link: '/pages/home',
      home: true,
    },
    {
      title: 'COMPONENTS.USER_MANAGEMENT',
      key: 'COMPONENTS.USER_MANAGEMENT',
      icon: 'person',
      children: [
        // {
        //   title: 'COMPONENTS.MY_PROFILE',
        //   key: 'COMPONENTS.MY_PROFILE',
        //   link: '/pages/user-management/profile',
        //   hidden: false
        // },
        {
          title: 'COMPONENTS.USER_LIST',
          key: 'COMPONENTS.USER_LIST',
          link: '/pages/user-management/users',
          hidden: false,
          guards: [IsAdmin]
        },
      ],
    },
    {
      title: 'COMPONENTS.STORE_MANAGEMENT',
      key: 'COMPONENTS.STORE_MANAGEMENT',
      icon: 'shopping-bag',
      link: '',
      hidden: false,
      guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminStore],
      children: [

        {
          title: 'COMPONENTS.STORES_LIST',
          key: 'COMPONENTS.STORES_LIST',
          link: '/pages/store-management/stores-list',
          hidden: false,
          guards: [IsAdmin]
        }
      ],
    },
    {
      title: 'COMPONENTS.CATALOUGE_MANAGEMENT',
      key: 'COMPONENTS.CATALOUGE_MANAGEMENT',
      icon: 'pricetags',
      hidden: false,
      guards: [IsAdminRetail, IsAdmin],
      children: [
        {
          title: 'COMPONENTS.CATEGORIES',
          key: 'COMPONENTS.CATEGORIES',
          hidden: false,
          guards: [isCategoryManagementVisible],
          children: [
            {
              title: 'COMPONENTS.CATEGORIES_LIST',
              key: 'COMPONENTS.CATEGORIES_LIST',
              link: '/pages/catalogue/categories/categories-list',
              guards: [isCategoryManagementVisible],
              hidden: false,
            },
            {
              title: 'COMPONENTS.CREATE_CATEGORY',
              key: 'COMPONENTS.CREATE_CATEGORY',
              link: '/pages/catalogue/categories/create-category',
              hidden: false,
              guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
            },
            {
              title: 'COMPONENTS.CATEGORIES_HIERARCHY',
              key: 'COMPONENTS.CATEGORIES_HIERARCHY',
              link: '/pages/catalogue/categories/categories-hierarchy',
              hidden: false,
              guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
            },
          ],
        },
        {
          title: 'COMPONENTS.PRODUCTS',
          key: 'COMPONENTS.PRODUCTS',
          hidden: false,
          guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
          children: [
            {
              title: 'COMPONENTS.PRODUCTS_LIST',
              key: 'COMPONENTS.PRODUCTS_LIST',
              link: '/pages/catalogue/products/products-list',
              hidden: false,
              guards: [IsAdminRetail]
            },
            {
              title: 'COMPONENTS.PRODUCT_ORDERING',
              key: 'COMPONENTS.PRODUCT_ORDERING',
              link: '/pages/catalogue/products/product-ordering',
              hidden: false,
              guards: [IsAdminRetail]
            }
          ],
        },
        {
          title: 'COMPONENTS.OPTIONS',
          key: 'COMPONENTS.OPTIONS',
          hidden: false,
          guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
          children: [
            {
              title: 'COMPONENTS.OPTIONS_LIST',
              key: 'COMPONENTS.OPTIONS_LIST',
              link: '/pages/catalogue/options/options-list',
              hidden: false,
              guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
            },
            {
              title: 'COMPONENTS.OPTIONS_VALUES_LIST',
              key: 'COMPONENTS.OPTIONS_VALUES_LIST',
              link: '/pages/catalogue/options/options-values-list',
              hidden: false,
              guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
            },
            {
              title: 'COMPONENTS.OPTION_SET_LIST',
              key: 'COMPONENTS.OPTION_SET_LIST',
              link: '/pages/catalogue/options/options-set-list',
              hidden: false,
              guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
            },
            {
              title: 'COMPONENTS.VARIATIONS_LIST',
              key: 'COMPONENTS.VARIATIONS_LIST',
              link: '/pages/catalogue/options/variations/list',
              hidden: false,
              guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
            },
          ]
        },
        {
          title: 'COMPONENTS.BRANDS',
          key: 'COMPONENTS.BRANDS',
          hidden: false,
          guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
          children: [
            {
              title: 'COMPONENTS.BRANDS_LIST',
              key: 'COMPONENTS.BRANDS_LIST',
              link: '/pages/catalogue/brands/brands-list',
              hidden: false,
              guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
            },
            {
              title: 'COMPONENTS.CREATE_BRAND',
              key: 'COMPONENTS.CREATE_BRAND',
              link: '/pages/catalogue/brands/create-brand',
              hidden: false,
              guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
            }
          ]
        },
        {
          title: 'COMPONENTS.PRODUCTS_GROUPS',
          key: 'COMPONENTS.PRODUCTS_GROUPS',
          hidden: false,
          guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
          children: [

            {
              title: 'COMPONENTS.PRODUCTS_GROUPS_LIST',
              key: 'COMPONENTS.PRODUCTS_GROUPS_LIST',
              link: '/pages/catalogue/products-groups/groups-list',
              hidden: false,
              guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
            },
            // {
            //   title: 'COMPONENTS.CREATE_PRODUCTS_GROUPS',
            //   key: 'COMPONENTS.CREATE_PRODUCTS_GROUPS',
            //   link: '/pages/catalogue/products-groups/create-products-group',
            //   hidden: false,
            //   guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
            // }
          ]
        },
        {
          title: 'COMPONENTS.PRODUCT_TYPES',
          key: 'COMPONENTS.PRODUCT_TYPES',
          hidden: false,
          guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
          children: [

            {
              title: 'PRODUCT_TYPE.PRODUCT_TYPE_LIST',
              key: 'PRODUCT_TYPE.PRODUCT_TYPE_LIST',
              link: '/pages/catalogue/types/types-list',
              hidden: false,
              guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
            }
          ]
        },

        /**
         {
         title: 'COMPONENTS.CATALOGUES',
         key: 'COMPONENTS.CATALOGUES',
         hidden: false,
         guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
         children: [

         {
         title: 'COMPONENTS.CATALOGUES_LIST',
         key: 'COMPONENTS.CATALOGUES_LIST',
         link: '/pages/catalogue/catalogues/catalogues-list',
         hidden: false,
         guards: [IsSuperadmin, IsAdmin, IsAdminRetail, IsAdminCatalogue],
         }
         ],
         },
         **/

      ]
    },
    {
      title: 'COMPONENTS.CONTENT_MANAGEMENT',
      key: 'COMPONENTS.CONTENT_MANAGEMENT',
      icon: 'edit-2',
      children: [
        {
          title: 'COMPONENTS.CONTENT_PAGES',
          key: 'COMPONENTS.CONTENT_PAGES',
          link: '/pages/content/pages/list',
        },
        {
          title: 'COMPONENTS.CONTENT_BOXES',
          key: 'COMPONENTS.CONTENT_BOXES',
          link: '/pages/content/boxes/list',
        },
        // {
        //   title: 'COMPONENTS.CONTENT_IMAGES',
        //   key: 'COMPONENTS.CONTENT_IMAGES',
        //   link: '/pages/content/images/list',
        // },
        {
          title: 'COMPONENTS.CONTENT_FILES',
          key: 'COMPONENTS.CONTENT_FILES',
          link: '/pages/content/files/list',
        },
        // {
        //   title: 'Promotion',
        //   key: 'sideNav.managecontent',
        //   link: '/pages/content/promotion',
        //   // link: '/pages/forms/datepicker',
        // },
      ],
    },
    {
      title: 'COMPONENTS.SHIPPING_MANAGEMENT',
      key: 'COMPONENTS.SHIPPING_MANAGEMENT',
      icon: 'car',
      children: [
        {
          title: 'SHIPPING.EXPEDITION',
          key: 'SHIPPING.EXPEDITION',
          link: '/pages/shipping/config',
        },
        {
          title: 'COMPONENTS.METHODS',
          key: 'COMPONENTS.METHODS',
          link: '/pages/shipping/methods',
        },
        {
          title: 'SHIPPING.ORIGIN',
          key: 'SHIPPING.ORIGIN',
          link: '/pages/shipping/origin',
        },
        {
          title: 'SHIPPING.PACKAGING',
          key: 'SHIPPING.PACKAGING',
          link: '/pages/shipping/packaging',
        }
        /**
         {
         title: 'COMPONENTS.RULES',
         key: 'COMPONENTS.RULES',
         link: '/pages/shipping/rules',
         },
         */

        // {
        //   title: 'Options',
        //   key: 'sideNav.options',
        //   link: '/pages/shipping/config',
        // },
        // {
        //   title: 'Packaging',
        //   key: 'sideNav.packaging',
        //   link: '/pages/shipping/config',
        // }
      ]
    },
    {
      title: 'COMPONENTS.PAYMENT',
      key: 'COMPONENTS.PAYMENT',
      icon: 'credit-card',
      link: '/pages/payment/methods'
    },
    {
      title: 'COMPONENTS.TAX_MANAGEMENT',
      key: 'COMPONENTS.TAX_MANAGEMENT',
      icon: 'file-text',
      children: [
        {
          title: 'COMPONENTS.TAX_CLASS',
          key: 'COMPONENTS.TAX_CLASS',
          link: '/pages/tax-management/classes-list'
        },
        {
          title: 'COMPONENTS.TAX_RATE',
          key: 'COMPONENTS.TAX_RATE',
          link: '/pages/tax-management/rate-list'
        }
      ]
    },
    // {
    //   title: 'Payment',
    //   // icon: 'fas fa-shopping-cart',
    //   link: '',
    // },
    {
      title: 'COMPONENTS.CUSTOMER_MANAGEMENT',
      key: 'COMPONENTS.CUSTOMER_MANAGEMENT',
      icon: 'people',
      children: [
        {
          title: 'COMPONENTS.CUSTOMER_LIST',
          key: 'COMPONENTS.CUSTOMER_LIST',
          link: '/pages/customer/list',
        },
        // {
        //   title: 'COMPONENTS.OPTIONS',
        //   key: 'COMPONENTS.OPTIONS',
        //   link: '/pages/customer/option/list',
        // },
        // {
        //   title: 'COMPONENTS.OPTIONS_VALUE',
        //   key: 'COMPONENTS.OPTIONS_VALUE',
        //   link: '/pages/customer/value/list',
        // },
        // {
        //   title: 'COMPONENTS.MANAGE_OPTIONS',
        //   key: 'COMPONENTS.MANAGE_OPTIONS',
        //   link: '/pages/customer/manage/list',
        // }
      ]
    },
    // {
    //   title: 'Shipping',
    //   // icon: 'fas fa-shopping-cart',
    //   link: '',
    // },
    // {
    //   title: 'Payment',
    //   // icon: 'fas fa-shopping-cart',
    //   link: '',
    // },
    // {
    //   title: 'Customers',
    //   // icon: 'fas fa-shopping-cart',
    //   link: '',
    // },
    {
      title: 'COMPONENTS.ORDER_MANAGEMENT',
      key: 'COMPONENTS.ORDER_MANAGEMENT',
      icon: 'shopping-cart',
      // link: '/pages/orders',
      // pathMatch: 'prefix',
      hidden: false,
      guards: [IsOrderManagementVisible],
      children: [
        {
          title: 'COMPONENTS.ORDERS',
          key: 'COMPONENTS.ORDERS',
          link: '/pages/orders',
          guards: [IsOrderManagementVisible]
        }
      ]
    },
    // {
    //   title: 'Manage taxes',
    //   // icon: 'fas fa-shopping-cart',
    //   link: '',
    // },
    // {
    //   title: 'Cache management',
    //   // icon: 'fas fa-shopping-cart',
    //   link: '',
    // },
    // {
    //   title: 'Security',
    //   // icon: 'fas fa-shopping-cart',
    //   link: '',
    // },
    // {
    //   title: 'Configurations',
    //   // icon: 'fas fa-shopping-cart',
    //   link: '',
    // },
  ];

