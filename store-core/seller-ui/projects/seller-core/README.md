# seller-core

Shared seller domain services, models, error handling and table state. Consumers must provide
`provideSellerCore(...)`; applications retain ownership of translations, including the `ERRORS.*`
keys required by `ApiErrorService`. After a fresh checkout run `npm run build:lib` before building
an application that resolves this package from `dist/`.
