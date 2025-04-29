const {shareAll, withModuleFederationPlugin} = require('@angular-architects/module-federation/webpack');

module.exports = withModuleFederationPlugin({

  name: 'merchant-ui',

  exposes: {
    './CatalogueModule': './src/app/pages/catalogue/catalogue.module.ts',
    './ContentModule': './src/app/pages/content/content.module.ts',
    './CustomersModule': './src/app/pages/customer/customer.module.ts',
    './OrdersModule': './src/app/pages/orders/orders.module.ts',
  },

  shared: {
    ...shareAll({singleton: true, strictVersion: true, requiredVersion: 'auto'}),
  },

});
