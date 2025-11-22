import {MenuItem} from './menu-item';
import {Roles} from "./shared/models/roles";

const CanAccessOrders = (roles: Roles) => {
  return roles.isOrgAdmin || roles.isStoreAdmin || roles.isStoreModerator;
};

const CanAccessHome = (roles: Roles) => {
  return true;
};

const CanAccessCatalogue = (roles: Roles) => {
  return roles.isOrgAdmin || roles.isStoreAdmin || roles.isStoreModerator;
};

const CanAccessCustomers = (roles: Roles) => {
  return roles.isOrgAdmin || roles.isStoreAdmin;
};

const CanAccessUsers = (roles: Roles) => {
  return roles.isOrgAdmin || roles.isStoreAdmin;
};

const CanAccessStores = (roles: Roles) => {
  return roles.isOrgAdmin;
};

const CanAccessAdmin = (roles: Roles) => {
  return roles.isSuperAdmin || roles.isSupport;
};

const CanAccessOrg = (roles: Roles) => {
  return roles.isSuperAdmin || roles.isSupport;
};

const CanAccessContent = (roles: Roles) => {
  return roles.isOrgAdmin || roles.isStoreAdmin;
};

const CanAccessSubscriptionAndUsage = (roles: Roles) => {
  return roles.isOrgAdmin;
};


export const MENU_ITEMS: MenuItem[] =
  [
    {
      title: 'COMPONENTS.HOME',
      key: 'COMPONENTS.HOME',
      icon: 'home',
      guards: [CanAccessHome],
      link: '/pages',
      home: true,
    },
    {
      title: 'COMPONENTS.USER_MANAGEMENT',
      key: 'COMPONENTS.USER_MANAGEMENT',
      icon: 'user-plus',
      guards: [CanAccessUsers],
      children: [
        {
          title: 'COMPONENTS.USER_LIST',
          key: 'COMPONENTS.USER_LIST',
          link: '/pages/user-management/users',
          hidden: false
        },
      ],
    },
    {
      title: 'COMPONENTS.STORE_MANAGEMENT',
      key: 'COMPONENTS.STORE_MANAGEMENT',
      icon: 'shopping-bag',
      link: '',
      hidden: false,
      guards: [CanAccessStores],
      children: [

        {
          title: 'COMPONENTS.STORES_LIST',
          key: 'COMPONENTS.STORES_LIST',
          link: '/pages/store-management/stores-list',
          hidden: false,
        }
      ],
    },
    {
      title: 'COMPONENTS.ORG_MANAGEMENT',
      key: 'COMPONENTS.ORG_MANAGEMENT',
      icon: 'shopping-bag',
      link: '',
      hidden: false,
      guards: [CanAccessOrg],
      children: [

        {
          title: 'COMPONENTS.ORG_LIST',
          key: 'COMPONENTS.ORG_LIST',
          link: '/pages/org-management/org-list',
          hidden: false,
        }
      ],
    },
    {
      title: 'COMPONENTS.CATALOUGE_MANAGEMENT',
      key: 'COMPONENTS.CATALOUGE_MANAGEMENT',
      icon: 'warehouse',
      hidden: false,
      guards: [CanAccessCatalogue],
      children: [
        {
          title: 'COMPONENTS.CATEGORIES',
          key: 'COMPONENTS.CATEGORIES',
          hidden: false,
          children: [
            {
              title: 'COMPONENTS.CATEGORIES_LIST',
              key: 'COMPONENTS.CATEGORIES_LIST',
              link: '/pages/catalogue/categories/categories-list',
              hidden: false,
            },
            {
              title: 'COMPONENTS.CATEGORIES_HIERARCHY',
              key: 'COMPONENTS.CATEGORIES_HIERARCHY',
              link: '/pages/catalogue/categories/categories-hierarchy',
              hidden: false,
            },
          ],
        },
        {
          title: 'COMPONENTS.PRODUCTS',
          key: 'COMPONENTS.PRODUCTS',
          hidden: false,
          children: [
            {
              title: 'COMPONENTS.PRODUCTS_LIST',
              key: 'COMPONENTS.PRODUCTS_LIST',
              link: '/pages/catalogue/products/products-list',
              hidden: false,
            },
            /*
                        {
                          title: 'COMPONENTS.PRODUCT_ORDERING',
                          key: 'COMPONENTS.PRODUCT_ORDERING',
                          link: '/pages/catalogue/products/product-ordering',
                          hidden: false,
                          guards: [IsAdminRetail]
                        }
            */
          ],
        },
        /*
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
        */
        {
          title: 'COMPONENTS.BRANDS',
          key: 'COMPONENTS.BRANDS',
          hidden: false,
          children: [
            {
              title: 'COMPONENTS.BRANDS_LIST',
              key: 'COMPONENTS.BRANDS_LIST',
              link: '/pages/catalogue/brands/brands-list',
              hidden: false,
            }
          ]
        },
        {
          title: 'COMPONENTS.PRODUCTS_GROUPS',
          key: 'COMPONENTS.PRODUCTS_GROUPS',
          hidden: false,
          children: [

            {
              title: 'COMPONENTS.PRODUCTS_GROUPS_LIST',
              key: 'COMPONENTS.PRODUCTS_GROUPS_LIST',
              link: '/pages/catalogue/products-groups/groups-list',
              hidden: false,
            }
          ]
        },
        {
          title: 'COMPONENTS.PRODUCT_TYPES',
          key: 'COMPONENTS.PRODUCT_TYPES',
          hidden: false,
          children: [

            {
              title: 'PRODUCT_TYPE.PRODUCT_TYPE_LIST',
              key: 'PRODUCT_TYPE.PRODUCT_TYPE_LIST',
              link: '/pages/catalogue/types/types-list',
              hidden: false,
            }
          ]
        }
      ]
    },
    {
      title: 'COMPONENTS.CONTENT_MANAGEMENT',
      key: 'COMPONENTS.CONTENT_MANAGEMENT',
      icon: 'arrows-to-circle',
      guards: [CanAccessContent],
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
        {
          title: 'COMPONENTS.CONTENT_FILES',
          key: 'COMPONENTS.CONTENT_FILES',
          link: '/pages/content/files/list',
        }
      ],
    },
    {
      title: 'COMPONENTS.CUSTOMER_MANAGEMENT',
      key: 'COMPONENTS.CUSTOMER_MANAGEMENT',
      icon: 'users-between-lines',
      guards: [CanAccessCustomers],
      children: [
        {
          title: 'COMPONENTS.CUSTOMER_LIST',
          key: 'COMPONENTS.CUSTOMER_LIST',
          link: '/pages/customer/list',
        }
      ]
    },
    {
      title: 'COMPONENTS.ORDER_MANAGEMENT',
      key: 'COMPONENTS.ORDER_MANAGEMENT',
      icon: 'shopping-cart',
      hidden: false,
      guards: [CanAccessOrders],
      children: [
        {
          title: 'COMPONENTS.ORDERS',
          key: 'COMPONENTS.ORDERS',
          link: '/pages/orders',
        }
      ]
    },
    {
      title: 'COMPONENTS.SUBSCRIPTION_AND_USAGE',
      key: 'COMPONENTS.SUBSCRIPTION_AND_USAGE',
      icon: 'calendar',
      hidden: false,
      guards: [CanAccessSubscriptionAndUsage],
      children: [
        {
          title: 'COMPONENTS.SUBSCRIPTION',
          key: 'COMPONENTS.SUBSCRIPTION',
          link: '/pages/subscription-and-usage/subscription',
        },
        {
          title: 'COMPONENTS.USAGE',
          key: 'COMPONENTS.USAGE',
          link: '/pages/subscription-and-usage/usage',
        }
      ]
    },
  ];

