
### Structure of the project

1. this project is a mono repo, which is a kind of splitting small microservices in one repo. Why?
    - it's a small project and will help us with fast development
    - will easily share common lib
    - easy to manage releasing and deployment because all subproject compatible with each other

2. we are using Gradle for everything, for example, we are building Docker images for Keycloak and angular and Next.js
   using Gradle
3. in `settings.gradle` every project that ends with `-service` is a Java microservice
4. in `settings.gradle` every project that ends with `-ui` is a frontend microservice

### Clusters

So we split the project into `2 main clusters` that can scale independently.

So you can have :

1. one `store-core-cluster` holds all system operations like auth, domains, register org, creating n stores
2. One or Many `store-pod-cluster` hold all e-commerce functions like Catalog, Shopping Cart, .... you can have one or
   many of these cluster types are based on your needs for example if you have a special store and want to deploy it in
   a separate cluster with different memory CPU specs and isolated from all stores in the systems, or just one for all
   stores

#### note -->

you still can scale every cluster services horizontal autoscale as normal by scaling number of instances,pods,services

### Microservices

##### --- store-core-cluster --

1. `store-core:welcome-ui` welcome page for the Saas that display provider info, contact, pricing, register
2. `store-core:store-ui` dashboard build using angular to manage org and stores
3. `store-core:auth` identity provider using keycloak that provides secure access to any microservice or any
   service-to-service call
4. `store-core:manager:manager-service` core for creating org, store and registering domain and domain ownership
   validation
5. `store-core:gateway:gateway-service` gateway that provide access to all `store core cluster` microservices

##### --- store-pod-cluster --

1. `store-pod:store-service`   responsible for all e-commerce functions like (Catalog, Shopping cart, Checkout, Merchant, Order, Customer)
2. `store-pod:landing-ui` server side rending that serve store
3. `store-pod:gateway:gateway-service`  gateway that provide access to all `store pod cluster` microservices specially
   it maps every domain like example.com to its corresponding store-id so example.com>>point-to-store-id>>store-id
4. `store-pod:gateway:store-pod-saas-gateway-service`  handle dynamic tls certificate for every store

### Installation

1. we require you hava `java 21` , `gradle-8.9` , `node v20` , `docker && docker compose`

### Demo and Deployment

there are 2 options to run cvhome saas project

1. AWS ECS FARGATE --> for prod [check here](https://github.com/cvhome-saas/cvhome-ecs-fargate-infra)
2. DOCKER COMPOSE --> for local , dev ,demo [check here](https://github.com/cvhome-saas/cvhome-docker-compose-infra)

##### but before we start to deploy keep in mind we are populating with some test data (org, stores , users , catalog)

1. keycloak system user sys-admin@mail.com admin

2.

| org                                         | store                                                                                                             | username                       | pass  | role            |
|---------------------------------------------|-------------------------------------------------------------------------------------------------------------------|--------------------------------|-------|-----------------|
| all                                         | all                                                                                                               | super-admin@mail.com           | admin | SUPER_ADMIN     |
| all                                         | all                                                                                                               | support@mail.com               | admin | SUPPORT         |
| ---org1---                                  |                                                                                                                   | org1-admin@mail.com            | admin | ORG_ADMIN       |
| `id = d1952c95-312e-4bb9-9a2d-b703d031276f` | ---store1---<br/>`id = 65f023632bc46470c104b76f`<br/>`domain = org1-store1.store-pod-saas-gateway-1.gateway.com`  | org1-store1-admin@mail.com     | admin | STORE_ADMIN     |
|                                             |                                                                                                                   | org1-store1-moderator@mail.com | admin | STORE_MODERATOR |
|                                             | ---store2--- <br/>`id = 65f023632bc46470c104b75f`<br/>`domain = org1-store2.store-pod-saas-gateway-1.gateway.com` | org1-store2-admin@mail.com     | admin | STORE_ADMIN     |
|                                             |                                                                                                                   | org1-store2-moderator@mail.com | admin | STORE_MODERATOR |
| ---org2---                                  |                                                                                                                   | org2-admin@mail.com            | admin | ORG_ADMIN       |
| `id = d1952c95-312e-4bb6-9a2d-b703d031276f` | ---store1---<br/>`id = 65f020632bc46470c104b76f`<br/>`domain = org2-store1.store-pod-saas-gateway-1.gateway.com`  | org2-store1-admin@mail.com     | admin | STORE_ADMIN     |
|                                             |                                                                                                                   | org2-store1-moderator@mail.com | admin | STORE_MODERATOR |
|                                             | ---store2---<br/>`id = 65f023632bc26470c104b75f`<br/>`domain = org2-store2.store-pod-saas-gateway-1.gateway.com`  | org2-store2-admin@mail.com     | admin | STORE_ADMIN     |
|                                             |                                                                                                                   | org2-store2-moderator@mail.com | admin | STORE_MODERATOR |

### DOCKER COMPOSE deployment options

